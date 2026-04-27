package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import jakarta.inject.Singleton;

@Singleton
public class LoggingTaskObserver implements TaskObserver {

    @Override
    public void onTaskStatusChanged(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        System.out.println(
                "Task #" + task.getId()
                        + " status changed: "
                        + oldStatus + " -> " + newStatus
        );
    }
}