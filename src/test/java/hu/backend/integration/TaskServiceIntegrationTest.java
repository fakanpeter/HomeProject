package hu.backend.integration;

import hu.backend.dto.TaskResponse;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskServiceIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldCancelCreatedTask() {
        Task task = createTask(TaskStatus.CREATED);

        TaskResponse response = taskService.cancel(task.getId());

        assertEquals(TaskStatus.CANCELLED, response.status());

        Task savedTask = findTask(task.getId());

        assertEquals(TaskStatus.CANCELLED, savedTask.getStatus());

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertEquals(1, logs.size());
        assertEquals(TaskStatus.CREATED, logs.get(0).getOldStatus());
        assertEquals(TaskStatus.CANCELLED, logs.get(0).getNewStatus());
    }

    @Test
    void shouldCancelRunningTask() {
        Task task = createTask(TaskStatus.RUNNING);

        TaskResponse response = taskService.cancel(task.getId());

        assertEquals(TaskStatus.CANCELLED, response.status());

        Task savedTask = findTask(task.getId());

        assertEquals(TaskStatus.CANCELLED, savedTask.getStatus());

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertEquals(1, logs.size());
        assertEquals(TaskStatus.RUNNING, logs.get(0).getOldStatus());
        assertEquals(TaskStatus.CANCELLED, logs.get(0).getNewStatus());
    }

    @Test
    void shouldNotCancelCompletedTask() {
        Task task = createTask(TaskStatus.COMPLETED);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskService.cancel(task.getId())
        );

        assertEquals("Task cannot be cancelled from status: COMPLETED", exception.getMessage());

        Task savedTask = findTask(task.getId());

        assertEquals(TaskStatus.COMPLETED, savedTask.getStatus());

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertEquals(0, logs.size());
    }

    @Test
    void shouldNotCancelCancelledTask() {
        Task task = createTask(TaskStatus.CANCELLED);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskService.cancel(task.getId())
        );

        assertEquals("Task cannot be cancelled from status: CANCELLED", exception.getMessage());

        Task savedTask = findTask(task.getId());

        assertEquals(TaskStatus.CANCELLED, savedTask.getStatus());

        var logs = taskAuditLogRepository.findByTaskId(task.getId());

        assertEquals(0, logs.size());
    }
}