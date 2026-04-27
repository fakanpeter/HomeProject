package hu.backend.observer;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Context
@AllArgsConstructor
public class ObserverInitializer {
    private final TaskStatusSubject taskStatusSubject;
    private final LoggingTaskObserver loggingTaskObserver;
    private final AuditTaskObserver auditTaskObserver;

    @PostConstruct
    public void init() {
        taskStatusSubject.addObserver(loggingTaskObserver);
        taskStatusSubject.addObserver(auditTaskObserver);
    }
}