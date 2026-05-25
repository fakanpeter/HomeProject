package hu.backend.event;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;

@Serdeable
public record TaskEvent(Long taskId, String title, TaskEventType type, LocalDateTime occurredAt) {
}
