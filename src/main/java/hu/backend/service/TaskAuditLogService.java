package hu.backend.service;

import hu.backend.dto.TaskAuditLogResponse;
import hu.backend.model.TaskAuditLog;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskAuditLogRepository;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.Cacheable;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Singleton
@AllArgsConstructor
public class TaskAuditLogService {

    private final TaskAuditLogRepository taskAuditLogRepository;

    @Cacheable("task-audit-logs-by-task")
    public List<TaskAuditLogResponse> findByTaskId(Long taskId) {
        log.info("Loading task audit logs from database. taskId={}", taskId);

        return taskAuditLogRepository.findByTaskId(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheInvalidate(value = "task-audit-logs-by-task", parameters = {"taskId"})
    public void createAuditLog(Long taskId, TaskStatus oldStatus, TaskStatus newStatus) {
        TaskAuditLog auditLog = TaskAuditLog.builder()
                .taskId(taskId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .build();

        taskAuditLogRepository.save(auditLog);

        log.info("Task audit log created and cache invalidated. taskId={}", taskId);
    }

    private TaskAuditLogResponse toResponse(TaskAuditLog auditLog) {
        return new TaskAuditLogResponse(
                auditLog.getId(),
                auditLog.getTaskId(),
                auditLog.getOldStatus(),
                auditLog.getNewStatus(),
                auditLog.getChangedAt()
        );
    }
}