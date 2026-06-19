package hu.backend.integration;

import hu.backend.dto.TaskRequest;
import hu.backend.dto.TaskResponse;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskControllerIntegrationTest extends AbstractIntegrationTest {

    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void shouldCreateTask() {
        try (HttpClient client = HttpClient.create(embeddedServer.getURL())) {
            TaskRequest request = new TaskRequest("Controller test task");

            TaskResponse response = client.toBlocking().retrieve(
                    HttpRequest.POST("/tasks", request),
                    TaskResponse.class
            );

            assertNotNull(response.id());
            assertEquals("Controller test task", response.title());
            assertTrue(
                    response.status() == TaskStatus.CREATED
                            || response.status() == TaskStatus.RUNNING
                            || response.status() == TaskStatus.COMPLETED
            );
        }
    }

    @Test
    void shouldFindTaskById() {
        Task task = createTask("Find by id task", TaskStatus.CREATED);

        try (HttpClient client = HttpClient.create(embeddedServer.getURL())) {
            TaskResponse response = client.toBlocking().retrieve(
                    HttpRequest.GET("/tasks/" + task.getId()),
                    TaskResponse.class
            );

            assertEquals(task.getId(), response.id());
            assertEquals("Find by id task", response.title());
            assertEquals(TaskStatus.CREATED, response.status());
        }
    }

    @Test
    void shouldFindAllTasks() {
        createTask("First task", TaskStatus.CREATED);
        createTask("Second task", TaskStatus.RUNNING);

        try (HttpClient client = HttpClient.create(embeddedServer.getURL())) {
            List<TaskResponse> response = client.toBlocking().retrieve(
                    HttpRequest.GET("/tasks"),
                    Argument.listOf(TaskResponse.class)
            );

            assertEquals(2, response.size());
        }
    }

    @Test
    void shouldUpdateTask() {
        Task task = createTask("Old title", TaskStatus.CREATED);

        try (HttpClient client = HttpClient.create(embeddedServer.getURL())) {
            TaskRequest request = new TaskRequest("Updated title");

            TaskResponse response = client.toBlocking().retrieve(
                    HttpRequest.PUT("/tasks/" + task.getId(), request),
                    TaskResponse.class
            );

            assertEquals(task.getId(), response.id());
            assertEquals("Updated title", response.title());

            Task savedTask = findTask(task.getId());

            assertEquals("Updated title", savedTask.getTitle());
        }
    }

    @Test
    void shouldDeleteTask() {
        Task task = createTask("Delete task", TaskStatus.CREATED);

        try (HttpClient client = HttpClient.create(embeddedServer.getURL())) {
            client.toBlocking().exchange(
                    HttpRequest.DELETE("/tasks/" + task.getId())
            );

            assertTrue(taskRepository.findById(task.getId()).isEmpty());
        }
    }

    @Test
    void shouldCancelTask() {
        Task task = createTask("Cancel task", TaskStatus.RUNNING);

        try (HttpClient client = HttpClient.create(embeddedServer.getURL())) {
            TaskResponse response = client.toBlocking().retrieve(
                    HttpRequest.POST("/tasks/" + task.getId() + "/cancel", ""),
                    TaskResponse.class
            );

            assertEquals(TaskStatus.CANCELLED, response.status());

            Task savedTask = findTask(task.getId());

            assertEquals(TaskStatus.CANCELLED, savedTask.getStatus());
        }
    }

    @Test
    void shouldReturnAuditLogs() {
        Task task = createTask("Audit task", TaskStatus.CREATED);

        taskStatusService.changeStatus(task, TaskStatus.RUNNING);

        try (HttpClient client = HttpClient.create(embeddedServer.getURL())) {
            var response = client.toBlocking().retrieve(
                    HttpRequest.GET("/tasks/" + task.getId() + "/audit-logs"),
                    Argument.listOf(hu.backend.dto.TaskAuditLogResponse.class)
            );

            assertEquals(1, response.size());
            assertEquals(TaskStatus.CREATED, response.get(0).oldStatus());
            assertEquals(TaskStatus.RUNNING, response.get(0).newStatus());
        }
    }
}