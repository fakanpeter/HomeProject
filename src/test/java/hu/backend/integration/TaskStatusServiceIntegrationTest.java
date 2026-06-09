package hu.backend.integration;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskStatusServiceIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldChangeStatusAndCreateAuditLog() {
        Task task = createTask(TaskStatus.CREATED);

        Task updatedTask = taskStatusService.changeStatus(task, TaskStatus.RUNNING);

        assertEquals(TaskStatus.RUNNING, updatedTask.getStatus());
        assertNotNull(updatedTask.getStartedAt());

        Task savedTask = findTask(task.getId());

        assertEquals(TaskStatus.RUNNING, savedTask.getStatus());
        assertNotNull(savedTask.getStartedAt());

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertEquals(1, logs.size());
        assertEquals(TaskStatus.CREATED, logs.get(0).getOldStatus());
        assertEquals(TaskStatus.RUNNING, logs.get(0).getNewStatus());
    }

    @Test
    void shouldCompleteRunningTaskAndCreateAuditLog() {
        Task task = createTask(TaskStatus.RUNNING);

        Task result = taskStatusService.completeIfRunning(task.getId());

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getFinishedAt());

        Task savedTask = findTask(task.getId());

        assertEquals(TaskStatus.COMPLETED, savedTask.getStatus());
        assertNotNull(savedTask.getFinishedAt());

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertEquals(1, logs.size());
        assertEquals(TaskStatus.RUNNING, logs.get(0).getOldStatus());
        assertEquals(TaskStatus.COMPLETED, logs.get(0).getNewStatus());
    }

    @Test
    void shouldNotCompleteCancelledTask() {
        Task task = createTask(TaskStatus.CANCELLED);

        Task result = taskStatusService.completeIfRunning(task.getId());

        assertEquals(TaskStatus.CANCELLED, result.getStatus());

        Task savedTask = findTask(task.getId());

        assertEquals(TaskStatus.CANCELLED, savedTask.getStatus());

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertTrue(logs.isEmpty());
    }
}