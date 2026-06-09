package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskAuditLog;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditTaskObserverTest {

    @Mock
    TaskAuditLogRepository taskAuditLogRepository;

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

        ArgumentCaptor<TaskAuditLog> captor = ArgumentCaptor.forClass(TaskAuditLog.class);

        verify(taskAuditLogRepository).save(captor.capture());

        TaskAuditLog savedLog = captor.getValue();

        assertEquals(1L, savedLog.getTaskId());
        assertEquals(TaskStatus.CREATED, savedLog.getOldStatus());
        assertEquals(TaskStatus.RUNNING, savedLog.getNewStatus());
        assertNotNull(savedLog.getChangedAt());
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

        ArgumentCaptor<TaskAuditLog> captor = ArgumentCaptor.forClass(TaskAuditLog.class);

        verify(taskAuditLogRepository).save(captor.capture());

        TaskAuditLog savedLog = captor.getValue();

        assertEquals(1L, savedLog.getTaskId());
        assertEquals(TaskStatus.RUNNING, savedLog.getOldStatus());
        assertEquals(TaskStatus.CANCELLED, savedLog.getNewStatus());
        assertNotNull(savedLog.getChangedAt());
    }
}