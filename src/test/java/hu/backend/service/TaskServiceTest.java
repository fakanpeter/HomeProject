package hu.backend.service;

import hu.backend.dto.TaskRequest;
import hu.backend.dto.TaskResponse;
import hu.backend.kafka.TaskEventProducer;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    TaskEventProducer taskEventProducer;

    @Mock
    TaskStatusService taskStatusService;

    @InjectMocks
    TaskService taskService;

    @Test
    void shouldCreateTask() {
        TaskRequest request = new TaskRequest("New task");

        Task savedTask = Task.builder()
                .id(1L)
                .title("New task")
                .status(TaskStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.create(request);

        assertEquals(1L, response.id());
        assertEquals("New task", response.title());
        assertEquals(TaskStatus.CREATED, response.status());

        verify(taskRepository).save(any(Task.class));
        verify(taskEventProducer).publishTaskCreatedEvent(savedTask);
    }

    @Test
    void shouldFindTaskById() {
        Task task = taskWithStatus(TaskStatus.CREATED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Test task", response.title());
        assertEquals(TaskStatus.CREATED, response.status());

        verify(taskRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenFindingMissingTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.findById(1L)
        );

        assertEquals("Task not found with id: 1", exception.getMessage());

        verify(taskRepository).findById(1L);
    }

    @Test
    void shouldFindAllTasks() {
        Task firstTask = taskWithStatus(TaskStatus.CREATED);
        Task secondTask = Task.builder()
                .id(2L)
                .title("Second task")
                .status(TaskStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskRepository.findAll()).thenReturn(List.of(firstTask, secondTask));

        List<TaskResponse> response = taskService.findAll();

        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals(2L, response.get(1).id());

        verify(taskRepository).findAll();
    }

    @Test
    void shouldUpdateTaskTitle() {
        Task existingTask = taskWithStatus(TaskStatus.CREATED);

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Updated task")
                .status(TaskStatus.CREATED)
                .createdAt(existingTask.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        TaskRequest request = new TaskRequest("Updated task");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.update(existingTask)).thenReturn(updatedTask);

        TaskResponse response = taskService.update(1L, request);

        assertEquals(1L, response.id());
        assertEquals("Updated task", response.title());
        assertEquals(TaskStatus.CREATED, response.status());

        verify(taskRepository).findById(1L);
        verify(taskRepository).update(existingTask);
    }

    @Test
    void shouldThrowWhenUpdatingMissingTask() {
        TaskRequest request = new TaskRequest("Updated task");

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.update(1L, request)
        );

        assertEquals("Task not found with id: 1", exception.getMessage());

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).update(any(Task.class));
    }

    @Test
    void shouldDeleteTaskById() {
        taskService.delete(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void shouldDeleteAllTasks() {
        taskService.deleteAll();

        verify(taskRepository).deleteAll();
    }

    @Test
    void shouldCancelCreatedTask() {
        Task task = taskWithStatus(TaskStatus.CREATED);
        Task cancelledTask = taskWithStatus(TaskStatus.CANCELLED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskStatusService.changeStatus(task, TaskStatus.CANCELLED)).thenReturn(cancelledTask);

        TaskResponse response = taskService.cancel(1L);

        assertEquals(TaskStatus.CANCELLED, response.status());

        verify(taskRepository).findById(1L);
        verify(taskStatusService).changeStatus(task, TaskStatus.CANCELLED);
    }

    @Test
    void shouldCancelRunningTask() {
        Task task = taskWithStatus(TaskStatus.RUNNING);
        Task cancelledTask = taskWithStatus(TaskStatus.CANCELLED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskStatusService.changeStatus(task, TaskStatus.CANCELLED)).thenReturn(cancelledTask);

        TaskResponse response = taskService.cancel(1L);

        assertEquals(TaskStatus.CANCELLED, response.status());

        verify(taskRepository).findById(1L);
        verify(taskStatusService).changeStatus(task, TaskStatus.CANCELLED);
    }

    @Test
    void shouldNotCancelCompletedTask() {
        Task task = taskWithStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskService.cancel(1L)
        );

        assertEquals("Task cannot be cancelled from status: COMPLETED", exception.getMessage());

        verify(taskRepository).findById(1L);
        verify(taskStatusService, never()).changeStatus(any(Task.class), any(TaskStatus.class));
    }

    @Test
    void shouldNotCancelFailedTask() {
        Task task = taskWithStatus(TaskStatus.FAILED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskService.cancel(1L)
        );

        assertEquals("Task cannot be cancelled from status: FAILED", exception.getMessage());

        verify(taskRepository).findById(1L);
        verify(taskStatusService, never()).changeStatus(any(Task.class), any(TaskStatus.class));
    }

    @Test
    void shouldNotCancelAlreadyCancelledTask() {
        Task task = taskWithStatus(TaskStatus.CANCELLED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskService.cancel(1L)
        );

        assertEquals("Task cannot be cancelled from status: CANCELLED", exception.getMessage());

        verify(taskRepository).findById(1L);
        verify(taskStatusService, never()).changeStatus(any(Task.class), any(TaskStatus.class));
    }

    @Test
    void shouldThrowWhenCancellingMissingTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.cancel(1L)
        );

        assertEquals("Task not found with id: 1", exception.getMessage());

        verify(taskRepository).findById(1L);
        verify(taskStatusService, never()).changeStatus(any(Task.class), any(TaskStatus.class));
    }

    private Task taskWithStatus(TaskStatus status) {
        return Task.builder()
                .id(1L)
                .title("Test task")
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}