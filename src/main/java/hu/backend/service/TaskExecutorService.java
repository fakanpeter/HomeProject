package hu.backend.service;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.observer.TaskStatusSubject;
import hu.backend.repository.TaskRepository;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

@Singleton
public class TaskExecutorService {
    private final ExecutorService executorService;
    private final TaskRepository taskRepository;
    private final TaskStatusSubject taskStatusSubject;

    public TaskExecutorService(
            @Named(TaskExecutors.BLOCKING) ExecutorService executorService,
            TaskRepository taskRepository,
            TaskStatusSubject taskStatusSubject
    ) {
        this.executorService = executorService;
        this.taskRepository = taskRepository;
        this.taskStatusSubject = taskStatusSubject;
    }

    public void executeAsync(Long taskId) {
        executorService.submit(() -> executeTask(taskId));
    }

    private void executeTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        try {
            changeStatus(task, TaskStatus.RUNNING);

            Thread.sleep(5000);

            changeStatus(task, TaskStatus.COMPLETED);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            changeStatus(task, TaskStatus.FAILED);
        }
    }

    private void changeStatus(Task task, TaskStatus newStatus) {
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);

        if (newStatus == TaskStatus.RUNNING) {
            task.setStartedAt(LocalDateTime.now());
        }

        if (newStatus == TaskStatus.COMPLETED || newStatus == TaskStatus.FAILED) {
            task.setFinishedAt(LocalDateTime.now());
        }

        Task updatedTask = taskRepository.update(task);
        taskStatusSubject.notifyObservers(updatedTask, oldStatus, newStatus);
    }
}