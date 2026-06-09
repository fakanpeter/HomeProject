package hu.backend.service;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.observer.TaskStatusSubject;
import hu.backend.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskStatusServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    TaskStatusSubject taskStatusSubject;

    @InjectMocks
    TaskStatusService taskStatusService;

    @Test
    void shouldChangeStatusToRunningAndSetStartedAt() {
        Task task = Task.builder()
                .id(1L)
                .title("Test task")
                .status(TaskStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskRepository.update(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskStatusService.changeStatus(task, TaskStatus.RUNNING);

        assertEquals(TaskStatus.RUNNING, result.getStatus());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getUpdatedAt());
        assertNull(result.getFinishedAt());

        verify(taskRepository).update(task);
        verify(taskStatusSubject).notifyObservers(
                task,
                TaskStatus.CREATED,
                TaskStatus.RUNNING
        );
    }

    @Test
    void shouldChangeStatusToCompletedAndSetFinishedAt() {
        Task task = Task.builder()
                .id(1L)
                .title("Test task")
                .status(TaskStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();

        when(taskRepository.update(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskStatusService.changeStatus(task, TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getFinishedAt());
        assertNotNull(result.getUpdatedAt());

        verify(taskRepository).update(task);
        verify(taskStatusSubject).notifyObservers(
                task,
                TaskStatus.RUNNING,
                TaskStatus.COMPLETED
        );
    }

    @Test
    void shouldChangeStatusToCancelledAndSetFinishedAt() {
        Task task = Task.builder()
                .id(1L)
                .title("Test task")
                .status(TaskStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();

        when(taskRepository.update(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskStatusService.changeStatus(task, TaskStatus.CANCELLED);

        assertEquals(TaskStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getFinishedAt());
        assertNotNull(result.getUpdatedAt());

        verify(taskRepository).update(task);
        verify(taskStatusSubject).notifyObservers(
                task,
                TaskStatus.RUNNING,
                TaskStatus.CANCELLED
        );
    }

    @Test
    void shouldCompleteRunningTask() {
        Task task = Task.builder()
                .id(1L)
                .title("Running task")
                .status(TaskStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.update(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskStatusService.completeIfRunning(1L);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getFinishedAt());

        verify(taskRepository).findById(1L);
        verify(taskRepository).update(task);
        verify(taskStatusSubject).notifyObservers(
                task,
                TaskStatus.RUNNING,
                TaskStatus.COMPLETED
        );
    }

    @Test
    void shouldNotCompleteCancelledTask() {
        Task task = Task.builder()
                .id(1L)
                .title("Cancelled task")
                .status(TaskStatus.CANCELLED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskStatusService.completeIfRunning(1L);

        assertEquals(TaskStatus.CANCELLED, result.getStatus());

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).update(any(Task.class));
        verify(taskStatusSubject, never()).notifyObservers(any(), any(), any());
    }

    @Test
    void shouldNotCompleteCreatedTask() {
        Task task = Task.builder()
                .id(1L)
                .title("Created task")
                .status(TaskStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskStatusService.completeIfRunning(1L);

        assertEquals(TaskStatus.CREATED, result.getStatus());

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).update(any(Task.class));
        verify(taskStatusSubject, never()).notifyObservers(any(), any(), any());
    }

    @Test
    void shouldThrowWhenCompletingMissingTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatusService.completeIfRunning(1L)
        );

        assertEquals("Task not found with id: 1", exception.getMessage());

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).update(any(Task.class));
        verify(taskStatusSubject, never()).notifyObservers(any(), any(), any());
    }

    @Test
    void shouldChangeStatusToFailedAndSetFinishedAt() {
        Task task = Task.builder()
                .id(1L)
                .title("Test task")
                .status(TaskStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();

        when(taskRepository.update(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskStatusService.changeStatus(task, TaskStatus.FAILED);

        assertEquals(TaskStatus.FAILED, result.getStatus());
        assertNotNull(result.getFinishedAt());
        assertNotNull(result.getUpdatedAt());

        verify(taskRepository).update(task);
        verify(taskStatusSubject).notifyObservers(
                task,
                TaskStatus.RUNNING,
                TaskStatus.FAILED
        );
    }
}