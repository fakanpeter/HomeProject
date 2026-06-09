package hu.backend.integration;

import hu.backend.dto.TaskRequest;
import hu.backend.dto.TaskResponse;
import hu.backend.kafka.TaskEventProducer;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class TaskServiceCreateIntegrationTest extends AbstractIntegrationTest {

    @Inject
    TaskEventProducer taskEventProducer;

    @Test
    void shouldCreateTaskAndPublishCreatedEvent() {
        TaskRequest request = new TaskRequest("Created from integration test");

        TaskResponse response = taskService.create(request);

        assertNotNull(response.id());
        assertEquals("Created from integration test", response.title());
        assertEquals(TaskStatus.CREATED, response.status());

        Task savedTask = findTask(response.id());

        assertEquals("Created from integration test", savedTask.getTitle());
        assertEquals(TaskStatus.CREATED, savedTask.getStatus());
        assertNotNull(savedTask.getCreatedAt());

        verify(taskEventProducer).publishTaskCreatedEvent(any(Task.class));
    }

    @MockBean(TaskEventProducer.class)
    TaskEventProducer taskEventProducer() {
        return Mockito.mock(TaskEventProducer.class);
    }
}