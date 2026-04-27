package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;

public interface TaskObserver {
    void onTaskStatusChanged(Task task, TaskStatus oldStatus, TaskStatus newStatus);
}