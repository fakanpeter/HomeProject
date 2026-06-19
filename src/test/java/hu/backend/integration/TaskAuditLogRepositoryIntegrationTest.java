package hu.backend.integration;

import hu.backend.model.Task;
import hu.backend.model.TaskAuditLog;
import hu.backend.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskAuditLogRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldSaveAuditLog() {
        Task task = createTask(TaskStatus.CREATED);

        TaskAuditLog auditLog = TaskAuditLog.builder()
                .taskId(task.getId())
                .oldStatus(TaskStatus.CREATED)
                .newStatus(TaskStatus.RUNNING)
                .changedAt(LocalDateTime.now())
                .build();

        taskAuditLogRepository.save(auditLog);

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertEquals(1, logs.size());
        assertEquals(task.getId(), logs.get(0).getTaskId());
        assertEquals(TaskStatus.CREATED, logs.get(0).getOldStatus());
        assertEquals(TaskStatus.RUNNING, logs.get(0).getNewStatus());
        assertNotNull(logs.get(0).getChangedAt());
    }

    @Test
    void shouldFindAuditLogsByTaskId() {
        Task task = createTask(TaskStatus.CREATED);

        taskAuditLogRepository.save(TaskAuditLog.builder()
                .taskId(task.getId())
                .oldStatus(TaskStatus.CREATED)
                .newStatus(TaskStatus.RUNNING)
                .changedAt(LocalDateTime.now())
                .build());

        taskAuditLogRepository.save(TaskAuditLog.builder()
                .taskId(task.getId())
                .oldStatus(TaskStatus.RUNNING)
                .newStatus(TaskStatus.COMPLETED)
                .changedAt(LocalDateTime.now())
                .build());

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertEquals(2, logs.size());
        assertEquals(TaskStatus.CREATED, logs.get(0).getOldStatus());
        assertEquals(TaskStatus.RUNNING, logs.get(0).getNewStatus());
        assertEquals(TaskStatus.RUNNING, logs.get(1).getOldStatus());
        assertEquals(TaskStatus.COMPLETED, logs.get(1).getNewStatus());
    }

    @Test
    void shouldReturnEmptyListWhenTaskHasNoAuditLogs() {
        Task task = createTask(TaskStatus.CREATED);

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertTrue(logs.isEmpty());
    }

    @Test
    void shouldDeleteAllAuditLogs() {
        Task task = createTask(TaskStatus.CREATED);

        taskAuditLogRepository.save(TaskAuditLog.builder()
                .taskId(task.getId())
                .oldStatus(TaskStatus.CREATED)
                .newStatus(TaskStatus.RUNNING)
                .changedAt(LocalDateTime.now())
                .build());

        taskAuditLogRepository.deleteAll();

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertTrue(logs.isEmpty());
    }
}