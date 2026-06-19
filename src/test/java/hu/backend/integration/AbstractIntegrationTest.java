package hu.backend.integration;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskAuditLogRepository;
import hu.backend.repository.TaskRepository;
import hu.backend.service.TaskService;
import hu.backend.service.TaskStatusService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

@MicronautTest(environments = "test")
public abstract class AbstractIntegrationTest {

    @Inject
    protected TaskRepository taskRepository;

    @Inject
    protected TaskAuditLogRepository taskAuditLogRepository;

    @Inject
    protected TaskService taskService;

    @Inject
    protected TaskStatusService taskStatusService;

    public static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.9.1"));

    static {
        if (!KAFKA_CONTAINER.isRunning()) {
            KAFKA_CONTAINER.start();
        }

        System.setProperty("micronaut.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());
        System.setProperty("kafka.bootstrap.servers", KAFKA_CONTAINER.getBootstrapServers());
    }

    @BeforeEach
    void cleanDatabase() {
        taskAuditLogRepository.deleteAll();
        taskRepository.deleteAll();
    }

    protected Task createTask(TaskStatus status) {
        return createTask("Integration test task", status);
    }

    protected Task createTask(String title, TaskStatus status) {
        Task task = Task.builder()
                .title(title)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return taskRepository.save(task);
    }

    protected Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("Task not found with id: " + taskId));
    }
}