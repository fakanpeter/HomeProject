package hu.backend.dto;

import hu.backend.model.TaskAuditLog;
import hu.backend.model.TaskStatus;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;

@Serdeable
public record TaskAuditLogResponse(
        Long id,
        Long taskId,
        TaskStatus oldStatus,
        TaskStatus newStatus,
        LocalDateTime changedAt) {

    public static TaskAuditLogResponse from(TaskAuditLog log) {
        return new TaskAuditLogResponse(
                log.getId(),
                log.getTaskId(),
                log.getOldStatus(),
                log.getNewStatus(),
                log.getChangedAt()
        );
    }
}