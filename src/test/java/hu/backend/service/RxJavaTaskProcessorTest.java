package hu.backend.service;

import hu.backend.event.TaskEvent;
import hu.backend.event.TaskEventType;
import hu.backend.exception.TaskNotFoundException;
import hu.backend.kafka.TaskEventProducer;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskRepository;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RxJavaTaskProcessorTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    TaskStatusService taskStatusService;

    @Mock
    TaskEventProducer taskEventProducer;

    @InjectMocks
    RxJavaTaskProcessor rxJavaTaskProcessor;

    @BeforeEach
    void setUpSchedulers() {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @AfterEach
    void tearDownSchedulers() {
        RxJavaPlugins.reset();
    }

    @Test
    void shouldProcessCreatedTaskSuccessfully() {
        TaskEvent event = taskEvent(TaskEventType.TASK_CREATED);

        Task createdTask = taskWithStatus(TaskStatus.CREATED);
        Task runningTask = taskWithStatus(TaskStatus.RUNNING);
        Task completedTask = taskWithStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(createdTask));
        when(taskStatusService.changeStatus(createdTask, TaskStatus.RUNNING)).thenReturn(runningTask);
        when(taskStatusService.completeIfRunning(1L)).thenReturn(completedTask);

        TestObserver<Task> observer = rxJavaTaskProcessor.process(event).test();

        observer.assertComplete();
        observer.assertNoErrors();
        observer.assertValue(task -> task.getStatus() == TaskStatus.COMPLETED);

        verify(taskRepository).findById(1L);
        verify(taskStatusService).changeStatus(createdTask, TaskStatus.RUNNING);
        verify(taskStatusService).completeIfRunning(1L);
        verify(taskEventProducer).publishTaskCompleted(completedTask);
        verify(taskEventProducer, never()).publishTaskFailed(any(Task.class));
    }

    @Test
    void shouldNotPublishCompletedEventWhenTaskWasCancelled() {
        TaskEvent event = taskEvent(TaskEventType.TASK_CREATED);

        Task createdTask = taskWithStatus(TaskStatus.CREATED);
        Task runningTask = taskWithStatus(TaskStatus.RUNNING);
        Task cancelledTask = taskWithStatus(TaskStatus.CANCELLED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(createdTask));
        when(taskStatusService.changeStatus(createdTask, TaskStatus.RUNNING)).thenReturn(runningTask);
        when(taskStatusService.completeIfRunning(1L)).thenReturn(cancelledTask);

        TestObserver<Task> observer = rxJavaTaskProcessor.process(event).test();

        observer.assertComplete();
        observer.assertNoErrors();
        observer.assertValue(task -> task.getStatus() == TaskStatus.CANCELLED);

        verify(taskStatusService).changeStatus(createdTask, TaskStatus.RUNNING);
        verify(taskStatusService).completeIfRunning(1L);
        verify(taskEventProducer, never()).publishTaskCompleted(any(Task.class));
        verify(taskEventProducer, never()).publishTaskFailed(any(Task.class));
    }

    @Test
    void shouldMarkTaskAsFailedForUnsupportedEventType() {
        TaskEvent event = taskEvent(TaskEventType.TASK_COMPLETED);

        Task task = taskWithStatus(TaskStatus.RUNNING);
        Task failedTask = taskWithStatus(TaskStatus.FAILED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskStatusService.changeStatus(task, TaskStatus.FAILED)).thenReturn(failedTask);

        TestObserver<Task> observer = rxJavaTaskProcessor.process(event).test();

        observer.assertComplete();
        observer.assertNoErrors();
        observer.assertValue(result -> result.getStatus() == TaskStatus.FAILED);

        verify(taskRepository).findById(1L);
        verify(taskStatusService).changeStatus(task, TaskStatus.FAILED);
        verify(taskEventProducer).publishTaskFailed(failedTask);
        verify(taskEventProducer, never()).publishTaskCompleted(any(Task.class));
    }

    @Test
    void shouldFailWhenTaskDoesNotExist() {
        TaskEvent event = taskEvent(TaskEventType.TASK_CREATED);

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        TestObserver<Task> observer = rxJavaTaskProcessor.process(event).test();

        observer.assertError(TaskNotFoundException.class);
        observer.assertError(error -> error.getMessage().equals("Task not found with id: 1"));
        observer.assertNotComplete();

        verify(taskRepository).findById(1L);
        verify(taskStatusService, never()).changeStatus(any(Task.class), any(TaskStatus.class));
        verify(taskStatusService, never()).completeIfRunning(any(Long.class));
        verify(taskEventProducer, never()).publishTaskCompleted(any(Task.class));
        verify(taskEventProducer, never()).publishTaskFailed(any(Task.class));
    }

    private TaskEvent taskEvent(TaskEventType type) {
        return new TaskEvent(
                1L,
                "Test task",
                type,
                LocalDateTime.now()
        );
    }

    private Task taskWithStatus(TaskStatus status) {
        return Task.builder()
                .id(1L)
                .title("Test task")
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}