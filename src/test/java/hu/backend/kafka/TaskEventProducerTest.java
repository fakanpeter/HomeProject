package hu.backend.kafka;

import hu.backend.event.TaskEvent;
import hu.backend.event.TaskEventType;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskEventProducerTest {

    @Mock
    TaskEventClient taskEventClient;

    @InjectMocks
    TaskEventProducer taskEventProducer;

    @Test
    void shouldPublishTaskCreatedEvent() {
        Task task = taskWithStatus(TaskStatus.CREATED);

        taskEventProducer.publishTaskCreatedEvent(task);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);

        verify(taskEventClient).send(eq(1L), eventCaptor.capture());

        TaskEvent event = eventCaptor.getValue();

        assertEquals(1L, event.taskId());
        assertEquals("Test task", event.title());
        assertEquals(TaskEventType.TASK_CREATED, event.type());
        assertNotNull(event.occurredAt());
    }

    @Test
    void shouldPublishTaskCompletedEvent() {
        Task task = taskWithStatus(TaskStatus.COMPLETED);

        taskEventProducer.publishTaskCompleted(task);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);

        verify(taskEventClient).send(eq(1L), eventCaptor.capture());

        TaskEvent event = eventCaptor.getValue();

        assertEquals(1L, event.taskId());
        assertEquals("Test task", event.title());
        assertEquals(TaskEventType.TASK_COMPLETED, event.type());
        assertNotNull(event.occurredAt());
    }

    @Test
    void shouldPublishTaskFailedEvent() {
        Task task = taskWithStatus(TaskStatus.FAILED);

        taskEventProducer.publishTaskFailed(task);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);

        verify(taskEventClient).send(eq(1L), eventCaptor.capture());

        TaskEvent event = eventCaptor.getValue();

        assertEquals(1L, event.taskId());
        assertEquals("Test task", event.title());
        assertEquals(TaskEventType.TASK_FAILED, event.type());
        assertNotNull(event.occurredAt());
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