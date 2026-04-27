package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskAuditLog;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskAuditLogRepository;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Singleton
@AllArgsConstructor
public class AuditTaskObserver implements TaskObserver {
    private final TaskAuditLogRepository taskAuditLogRepository;

    @Override
    public void onTaskStatusChanged(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        TaskAuditLog log = TaskAuditLog.builder()
                .taskId(task.getId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .build();

        taskAuditLogRepository.save(log);
    }
}