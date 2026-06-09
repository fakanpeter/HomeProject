package hu.backend.controller;

import hu.backend.dto.TaskRequest;
import hu.backend.dto.TaskResponse;
import hu.backend.model.TaskStatus;
import hu.backend.service.TaskService;
import io.micronaut.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    TaskService taskService;

    @InjectMocks
    TaskController taskController;

    @Test
    void shouldCreateTask() {
        TaskRequest request = new TaskRequest("Controller unit task");

        LocalDateTime now = LocalDateTime.now();

        TaskResponse response = new TaskResponse(
                1L,
                "Controller unit task",
                TaskStatus.CREATED,
                now,
                now,
                null,
                null
        );

        when(taskService.create(request)).thenReturn(response);

        HttpResponse<TaskResponse> result = taskController.create(request);

        assertEquals(201, result.code());

        TaskResponse body = result.body();

        assertEquals(1L, body.id());
        assertEquals("Controller unit task", body.title());
        assertEquals(TaskStatus.CREATED, body.status());
        assertEquals(now, body.createdAt());
        assertEquals(now, body.updatedAt());
        assertNull(body.startedAt());
        assertNull(body.finishedAt());

        verify(taskService).create(request);
    }

    @Test
    void shouldFindAllTasks() {
        LocalDateTime now = LocalDateTime.now();

        TaskResponse firstResponse = new TaskResponse(
                1L,
                "First task",
                TaskStatus.CREATED,
                now,
                now,
                null,
                null
        );

        TaskResponse secondResponse = new TaskResponse(
                2L,
                "Second task",
                TaskStatus.RUNNING,
                now,
                now,
                now,
                null
        );

        when(taskService.findAll()).thenReturn(List.of(firstResponse, secondResponse));

        List<TaskResponse> result = taskController.findAll();

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).id());
        assertEquals("First task", result.get(0).title());
        assertEquals(TaskStatus.CREATED, result.get(0).status());

        assertEquals(2L, result.get(1).id());
        assertEquals("Second task", result.get(1).title());
        assertEquals(TaskStatus.RUNNING, result.get(1).status());

        verify(taskService).findAll();
    }

    @Test
    void shouldFindTaskById() {
        LocalDateTime now = LocalDateTime.now();

        TaskResponse response = new TaskResponse(
                1L,
                "Task",
                TaskStatus.CREATED,
                now,
                now,
                null,
                null
        );

        when(taskService.findById(1L)).thenReturn(response);

        TaskResponse result = taskController.findById(1L);

        assertEquals(1L, result.id());
        assertEquals("Task", result.title());
        assertEquals(TaskStatus.CREATED, result.status());
        assertEquals(now, result.createdAt());
        assertEquals(now, result.updatedAt());
        assertNull(result.startedAt());
        assertNull(result.finishedAt());

        verify(taskService).findById(1L);
    }

    @Test
    void shouldUpdateTask() {
        TaskRequest request = new TaskRequest("Updated task");

        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime updatedAt = LocalDateTime.now();

        TaskResponse response = new TaskResponse(
                1L,
                "Updated task",
                TaskStatus.CREATED,
                createdAt,
                updatedAt,
                null,
                null
        );

        when(taskService.update(1L, request)).thenReturn(response);

        TaskResponse result = taskController.update(1L, request);

        assertEquals(1L, result.id());
        assertEquals("Updated task", result.title());
        assertEquals(TaskStatus.CREATED, result.status());
        assertEquals(createdAt, result.createdAt());
        assertEquals(updatedAt, result.updatedAt());
        assertNull(result.startedAt());
        assertNull(result.finishedAt());

        verify(taskService).update(1L, request);
    }

    @Test
    void shouldDeleteTask() {
        taskController.delete(1L);

        verify(taskService).delete(1L);
    }

    @Test
    void shouldDeleteAllTasks() {
        taskController.deleteAll();

        verify(taskService).deleteAll();
    }

    @Test
    void shouldCancelTask() {
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(10);
        LocalDateTime updatedAt = LocalDateTime.now();
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime finishedAt = LocalDateTime.now();

        TaskResponse response = new TaskResponse(
                1L,
                "Task",
                TaskStatus.CANCELLED,
                createdAt,
                updatedAt,
                startedAt,
                finishedAt
        );

        when(taskService.cancel(1L)).thenReturn(response);

        TaskResponse result = taskController.cancel(1L);

        assertEquals(1L, result.id());
        assertEquals("Task", result.title());
        assertEquals(TaskStatus.CANCELLED, result.status());
        assertEquals(createdAt, result.createdAt());
        assertEquals(updatedAt, result.updatedAt());
        assertEquals(startedAt, result.startedAt());
        assertEquals(finishedAt, result.finishedAt());

        verify(taskService).cancel(1L);
    }
}