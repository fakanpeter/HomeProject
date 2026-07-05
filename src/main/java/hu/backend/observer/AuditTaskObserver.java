package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.service.TaskAuditLogService;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;

@Singleton
@AllArgsConstructor
public class AuditTaskObserver implements TaskObserver {

    private final TaskAuditLogService taskAuditLogService;

    @Override
    public void onTaskStatusChanged(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        taskAuditLogService.createAuditLog(task.getId(), oldStatus, newStatus);
    }
}