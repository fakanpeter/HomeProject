package hu.backend.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

import java.time.LocalDateTime;

@Serdeable
@Data
@AllArgsConstructor
@Builder
public class TaskAuditLog {
    private Long id;
    private Long taskId;
    private TaskStatus oldStatus;
    private TaskStatus newStatus;
    private LocalDateTime changedAt;
}
