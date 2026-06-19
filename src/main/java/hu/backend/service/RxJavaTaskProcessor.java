package hu.backend.service;

import hu.backend.event.TaskEvent;
import hu.backend.event.TaskEventType;
import hu.backend.exception.TaskNotFoundException;
import hu.backend.kafka.TaskEventProducer;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskRepository;
import io.micronaut.context.annotation.Value;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class RxJavaTaskProcessor {

    private final TaskRepository taskRepository;
    private final TaskStatusService taskStatusService;
    private final TaskEventProducer taskEventProducer;

    @Value("${task.processing.start-delay-millis:1000}")
    private long startDelayMillis;

    @Value("${task.processing.processing-delay-millis:5000}")
    private long processingDelayMillis;

    public RxJavaTaskProcessor(
            TaskRepository taskRepository,
            TaskStatusService taskStatusService,
            TaskEventProducer taskEventProducer
    ) {
        this.taskRepository = taskRepository;
        this.taskStatusService = taskStatusService;
        this.taskEventProducer = taskEventProducer;
    }

    public Single<Task> process(TaskEvent event) {
        return Single.just(event)
                .doOnSuccess(taskEvent -> log.info("RxJava pipeline started. event={}", taskEvent))
                .flatMap(this::validateCreatedEvent)
                .delay(startDelayMillis, TimeUnit.MILLISECONDS)
                .map(TaskEvent::taskId)
                .flatMap(this::findTaskById)
                .flatMap(task -> changeStatus(task, TaskStatus.RUNNING))
                .delay(processingDelayMillis, TimeUnit.MILLISECONDS)
                .flatMap(task -> completeIfRunning(task.getId()))
                .doOnSuccess(task -> {
                    if (task.getStatus() == TaskStatus.COMPLETED) {
                        taskEventProducer.publishTaskCompleted(task);
                        log.info("RxJava task processing completed. id={}", task.getId());
                    } else {
                        log.info(
                                "RxJava task processing finished without completion publish. id={}, status={}",
                                task.getId(),
                                task.getStatus()
                        );
                    }
                })
                .onErrorResumeNext(error -> handleFailure(event, error))
                .subscribeOn(Schedulers.io());
    }

    private Single<TaskEvent> validateCreatedEvent(TaskEvent event) {
        if (event.type() != TaskEventType.TASK_CREATED) {
            return Single.error(new IllegalArgumentException("Unsupported event type: " + event.type()));
        }

        return Single.just(event);
    }

    private Single<Task> findTaskById(Long taskId) {
        return Single.fromCallable(() -> taskRepository.findById(taskId)
                        .orElseThrow(() -> new TaskNotFoundException(taskId)))
                .subscribeOn(Schedulers.io());
    }

    private Single<Task> changeStatus(Task task, TaskStatus newStatus) {
        return Single.fromCallable(() -> taskStatusService.changeStatus(task, newStatus))
                .subscribeOn(Schedulers.io());
    }

    private Single<Task> completeIfRunning(Long taskId) {
        return Single.fromCallable(() -> taskStatusService.completeIfRunning(taskId))
                .subscribeOn(Schedulers.io());
    }

    private Single<Task> handleFailure(TaskEvent event, Throwable error) {
        log.error("RxJava task processing failed. taskId={}", event.taskId(), error);

        if (error instanceof TaskNotFoundException) {
            return Single.error(error);
        }

        return findTaskById(event.taskId())
                .flatMap(task -> changeStatus(task, TaskStatus.FAILED))
                .doOnSuccess(taskEventProducer::publishTaskFailed);
    }
}