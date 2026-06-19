package hu.backend.kafka;

import hu.backend.event.TaskEvent;
import hu.backend.event.TaskEventType;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.service.RxJavaTaskProcessor;
import io.reactivex.rxjava3.core.Single;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskEventConsumerTest {

    @Mock
    RxJavaTaskProcessor rxJavaTaskProcessor;

    @InjectMocks
    TaskEventConsumer taskEventConsumer;

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
    void shouldProcessTaskCreatedEvent() {
        TaskEvent event = taskEvent(TaskEventType.TASK_CREATED);
        Task completedTask = taskWithStatus(TaskStatus.COMPLETED);

        when(rxJavaTaskProcessor.process(event)).thenReturn(Single.just(completedTask));

        taskEventConsumer.receive(1L, event);

        verify(rxJavaTaskProcessor).process(event);
    }

    @Test
    void shouldIgnoreTaskCompletedEvent() {
        TaskEvent event = taskEvent(TaskEventType.TASK_COMPLETED);

        taskEventConsumer.receive(1L, event);

        verify(rxJavaTaskProcessor, never()).process(event);
    }

    @Test
    void shouldIgnoreTaskFailedEvent() {
        TaskEvent event = taskEvent(TaskEventType.TASK_FAILED);

        taskEventConsumer.receive(1L, event);

        verify(rxJavaTaskProcessor, never()).process(event);
    }

    @Test
    void shouldHandleProcessorError() {
        TaskEvent event = taskEvent(TaskEventType.TASK_CREATED);

        when(rxJavaTaskProcessor.process(event))
                .thenReturn(Single.error(new RuntimeException("Processing failed")));

        taskEventConsumer.receive(1L, event);

        verify(rxJavaTaskProcessor).process(event);
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