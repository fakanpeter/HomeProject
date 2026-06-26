package hu.backend.integration;

import hu.backend.exception.OptimisticLockingException;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldSaveAndFindTaskById() {
        Task task = Task.builder()
                .title("Repository test task")
                .status(TaskStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);

        assertNotNull(savedTask.getId());

        var foundTask = taskRepository.findById(savedTask.getId());

        assertTrue(foundTask.isPresent());
        assertEquals(savedTask.getId(), foundTask.get().getId());
        assertEquals("Repository test task", foundTask.get().getTitle());
        assertEquals(TaskStatus.CREATED, foundTask.get().getStatus());
    }

    @Test
    void shouldFindAllTasks() {
        createTask("First task", TaskStatus.CREATED);
        createTask("Second task", TaskStatus.RUNNING);

        var tasks = taskRepository.findAll();

        assertEquals(2, tasks.size());
    }

    @Test
    void shouldUpdateTask() {
        Task task = createTask(TaskStatus.CREATED);

        task.setTitle("Updated title");
        task.setStatus(TaskStatus.RUNNING);
        task.setStartedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.update(task);

        assertEquals("Updated title", updatedTask.getTitle());
        assertEquals(TaskStatus.RUNNING, updatedTask.getStatus());
        assertNotNull(updatedTask.getStartedAt());

        Task foundTask = findTask(task.getId());

        assertEquals("Updated title", foundTask.getTitle());
        assertEquals(TaskStatus.RUNNING, foundTask.getStatus());
        assertNotNull(foundTask.getStartedAt());
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = createTask(TaskStatus.CREATED);

        taskRepository.deleteById(task.getId());

        var foundTask = taskRepository.findById(task.getId());

        assertFalse(foundTask.isPresent());
    }

    @Test
    void shouldDeleteAllTasks() {
        createTask("First task", TaskStatus.CREATED);
        createTask("Second task", TaskStatus.RUNNING);

        taskRepository.deleteAll();

        var tasks = taskRepository.findAll();

        assertTrue(tasks.isEmpty());
    }

    @Test
    void shouldIncrementVersionWhenTaskIsUpdated() {
        Task task = Task.builder()
                .title("Versioned task")
                .status(TaskStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);

        assertEquals(0L, savedTask.getVersion());

        savedTask.setStatus(TaskStatus.RUNNING);

        Task updatedTask = taskRepository.update(savedTask);

        assertEquals(1L, updatedTask.getVersion());

        Task foundTask = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        assertEquals(1L, foundTask.getVersion());
        assertEquals(TaskStatus.RUNNING, foundTask.getStatus());
    }

    @Test
    void shouldRejectUpdateWithStaleVersion() {
        Task task = Task.builder()
                .title("Concurrent task")
                .status(TaskStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);

        Task firstCopy = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        Task secondCopy = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        firstCopy.setStatus(TaskStatus.RUNNING);
        taskRepository.update(firstCopy);

        secondCopy.setStatus(TaskStatus.CANCELLED);

        OptimisticLockingException exception = assertThrows(
                OptimisticLockingException.class,
                () -> taskRepository.update(secondCopy)
        );

        assertEquals(
                "Task was modified concurrently: " + savedTask.getId(),
                exception.getMessage()
        );

        Task foundTask = taskRepository.findById(savedTask.getId())
                .orElseThrow();

        assertEquals(TaskStatus.RUNNING, foundTask.getStatus());
        assertEquals(1L, foundTask.getVersion());
    }
}