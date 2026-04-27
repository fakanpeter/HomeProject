package hu.backend.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

import java.time.LocalDateTime;

@Serdeable
@Data
@Builder
@AllArgsConstructor
public class Task {
    private Long id;
    private String title;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}