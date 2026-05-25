package hu.backend.service;

import hu.backend.event.TaskEvent;
import hu.backend.event.TaskEventType;
import hu.backend.kafka.TaskEventProducer;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.observer.TaskStatusSubject;
import hu.backend.repository.TaskRepository;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class RxJavaTaskProcessor {

    private final TaskRepository taskRepository;
    private final TaskStatusSubject taskStatusSubject;
    private final TaskEventProducer taskEventProducer;

    public RxJavaTaskProcessor(
            TaskRepository taskRepository,
            TaskStatusSubject taskStatusSubject,
            TaskEventProducer taskEventProducer
    ) {
        this.taskRepository = taskRepository;
        this.taskStatusSubject = taskStatusSubject;
        this.taskEventProducer = taskEventProducer;
    }

    public Single<Task> process(TaskEvent event) {
        return Single.just(event)
                .doOnSuccess(taskEvent -> log.info("RxJava pipeline started. event={}", taskEvent))
                .flatMap(this::validateCreatedEvent)
                .map(TaskEvent::taskId)
                .flatMap(this::findTaskById)
                .flatMap(task -> changeStatus(task, TaskStatus.RUNNING))
                .delay(5, TimeUnit.SECONDS)
                .flatMap(task -> changeStatus(task, TaskStatus.COMPLETED))
                .doOnSuccess(task -> {
                    taskEventProducer.publishTaskCompleted(task);
                    log.info("RxJava task processing completed. id={}", task.getId());
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
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId)));
    }

    private Single<Task> changeStatus(Task task, TaskStatus newStatus) {
        return Single.fromCallable(() -> {
            TaskStatus oldStatus = task.getStatus();

            task.setStatus(newStatus);

            if (newStatus == TaskStatus.RUNNING && task.getStartedAt() == null) {
                task.setStartedAt(LocalDateTime.now());
            }

            if (newStatus == TaskStatus.COMPLETED || newStatus == TaskStatus.FAILED) {
                task.setFinishedAt(LocalDateTime.now());
            }

            Task updatedTask = taskRepository.update(task);
            taskStatusSubject.notifyObservers(updatedTask, oldStatus, newStatus);

            return updatedTask;
        });
    }

    private Single<Task> handleFailure(TaskEvent event, Throwable error) {
        log.error("RxJava task processing failed. taskId={}", event.taskId(), error);

        return findTaskById(event.taskId())
                .flatMap(task -> changeStatus(task, TaskStatus.FAILED))
                .doOnSuccess(taskEventProducer::publishTaskFailed);
    }

}
