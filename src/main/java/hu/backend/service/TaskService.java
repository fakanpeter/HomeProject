package hu.backend.service;

import hu.backend.dto.TaskAuditLogResponse;
import hu.backend.dto.TaskRequest;
import hu.backend.dto.TaskResponse;
import hu.backend.kafka.TaskEventClient;
import hu.backend.kafka.TaskEventProducer;
import hu.backend.model.Task;
import hu.backend.model.TaskAuditLog;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskAuditLogRepository;
import hu.backend.repository.TaskRepository;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Singleton
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskAuditLogRepository taskAuditLogRepository;
    private final TaskEventProducer taskEventProducer;

    public TaskResponse create(TaskRequest request) {
        Task task = Task.builder()
                .title(request.title())
                .status(TaskStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);
        taskEventProducer.publishTaskCreatedEvent(savedTask);
        return toResponse(savedTask);
    }

    public List<TaskResponse> findAll() {
        return taskRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse findById(Long id) {
        Task task = getTaskOrThrow(id);
        return toResponse(task);
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task task = getTaskOrThrow(id);
        task.setTitle(request.title());

        Task updatedTask = taskRepository.update(task);

        return toResponse(updatedTask);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    public void deleteAll() {
        taskRepository.deleteAll();
    }

    public List<TaskAuditLogResponse> findAuditLogsByTaskId(Long taskId) {
        return taskAuditLogRepository.findByTaskId(taskId)
                .stream()
                .map(this::toAuditLogResponse)
                .toList();
    }

    private Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.from(task);
    }

    private TaskAuditLogResponse toAuditLogResponse(TaskAuditLog log) {
       return TaskAuditLogResponse.from(log);
    }
}