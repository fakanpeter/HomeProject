package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LoggingTaskObserverTest {

    private final LoggingTaskObserver loggingTaskObserver = new LoggingTaskObserver();

    @Test
    void shouldHandleStatusChangeWithoutThrowingException() {
        Task task = Task.builder()
                .id(1L)
                .title("Logged task")
                .status(TaskStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> loggingTaskObserver.onTaskStatusChanged(
                task,
                TaskStatus.CREATED,
                TaskStatus.RUNNING
        ));
    }

    @Test
    void shouldHandleCancelledStatusChangeWithoutThrowingException() {
        Task task = Task.builder()
                .id(1L)
                .title("Cancelled task")
                .status(TaskStatus.CANCELLED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> loggingTaskObserver.onTaskStatusChanged(
                task,
                TaskStatus.RUNNING,
                TaskStatus.CANCELLED
        ));
    }
}