package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.service.TaskAuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditTaskObserverTest {

    @Mock
    TaskAuditLogService taskAuditLogService;

    @InjectMocks
    AuditTaskObserver auditTaskObserver;

    @Test
    void shouldSaveAuditLogWhenTaskStatusChanges() {
        Task task = Task.builder()
                .id(1L)
                .title("Observed task")
                .status(TaskStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        auditTaskObserver.onTaskStatusChanged(
                task,
                TaskStatus.CREATED,
                TaskStatus.RUNNING
        );

        verify(taskAuditLogService).createAuditLog(
                1L,
                TaskStatus.CREATED,
                TaskStatus.RUNNING
        );
    }

    @Test
    void shouldSaveAuditLogForCancelledStatus() {
        Task task = Task.builder()
                .id(1L)
                .title("Cancelled task")
                .status(TaskStatus.CANCELLED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        auditTaskObserver.onTaskStatusChanged(
                task,
                TaskStatus.RUNNING,
                TaskStatus.CANCELLED
        );

        verify(taskAuditLogService).createAuditLog(
                1L,
                TaskStatus.RUNNING,
                TaskStatus.CANCELLED
        );
    }
}