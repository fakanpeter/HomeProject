package hu.backend.service;

import hu.backend.model.TaskAuditLog;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskAuditLogRepository;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@MicronautTest(environments = "test")
class TaskAuditLogServiceCacheTest {

    @Inject
    TaskAuditLogService taskAuditLogService;

    @Inject
    TaskAuditLogRepository taskAuditLogRepository;

    @Test
    void shouldCacheAuditLogsByTaskId() {
        Long taskId = 1L;

        TaskAuditLog auditLog = TaskAuditLog.builder()
                .id(1L)
                .taskId(taskId)
                .oldStatus(TaskStatus.CREATED)
                .newStatus(TaskStatus.RUNNING)
                .changedAt(LocalDateTime.now())
                .build();

        when(taskAuditLogRepository.findByTaskId(taskId))
                .thenReturn(List.of(auditLog));

        taskAuditLogService.findByTaskId(taskId);
        taskAuditLogService.findByTaskId(taskId);

        verify(taskAuditLogRepository, times(1)).findByTaskId(taskId);
    }

    @Test
    void shouldInvalidateAuditLogCacheWhenNewAuditLogIsCreated() {
        Long taskId = 2L;

        TaskAuditLog firstAuditLog = TaskAuditLog.builder()
                .id(1L)
                .taskId(taskId)
                .oldStatus(TaskStatus.CREATED)
                .newStatus(TaskStatus.RUNNING)
                .changedAt(LocalDateTime.now())
                .build();

        TaskAuditLog secondAuditLog = TaskAuditLog.builder()
                .id(2L)
                .taskId(taskId)
                .oldStatus(TaskStatus.RUNNING)
                .newStatus(TaskStatus.COMPLETED)
                .changedAt(LocalDateTime.now())
                .build();

        when(taskAuditLogRepository.findByTaskId(taskId))
                .thenReturn(List.of(firstAuditLog))
                .thenReturn(List.of(firstAuditLog, secondAuditLog));

        taskAuditLogService.findByTaskId(taskId);
        taskAuditLogService.findByTaskId(taskId);

        verify(taskAuditLogRepository, times(1)).findByTaskId(taskId);

        taskAuditLogService.createAuditLog(
                taskId,
                TaskStatus.RUNNING,
                TaskStatus.COMPLETED
        );

        taskAuditLogService.findByTaskId(taskId);

        verify(taskAuditLogRepository, times(2)).findByTaskId(taskId);
        verify(taskAuditLogRepository).save(any(TaskAuditLog.class));
    }

    @MockBean(TaskAuditLogRepository.class)
    TaskAuditLogRepository taskAuditLogRepository() {
        return mock(TaskAuditLogRepository.class);
    }
}