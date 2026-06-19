package hu.backend.service;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.observer.TaskStatusSubject;
import hu.backend.repository.TaskRepository;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Singleton
@AllArgsConstructor
public class TaskStatusService {

    private final TaskRepository taskRepository;
    private final TaskStatusSubject taskStatusSubject;

    public Task changeStatus(Task task, TaskStatus newStatus) {
        TaskStatus oldStatus = task.getStatus();

        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());

        if (newStatus == TaskStatus.RUNNING && task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }

        if (isFinishedStatus(newStatus)) {
            task.setFinishedAt(LocalDateTime.now());
        }

        Task updatedTask = taskRepository.update(task);

        taskStatusSubject.notifyObservers(updatedTask, oldStatus, newStatus);

        return updatedTask;
    }

    public Task completeIfRunning(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        if (task.getStatus() != TaskStatus.RUNNING) {
            return task;
        }

        return changeStatus(task, TaskStatus.COMPLETED);
    }

    private boolean isFinishedStatus(TaskStatus status) {
        return status == TaskStatus.COMPLETED
                || status == TaskStatus.FAILED
                || status == TaskStatus.CANCELLED;
    }
}