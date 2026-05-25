package hu.backend.kafka;

import hu.backend.event.TaskEvent;
import hu.backend.event.TaskEventType;
import hu.backend.model.Task;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Singleton
public class TaskEventProducer {
   private final TaskEventClient taskEventClient;

    public TaskEventProducer(TaskEventClient taskEventClient) {
        this.taskEventClient = taskEventClient;
    }

    public void publishTaskCreatedEvent(Task task) {
        TaskEvent event = new TaskEvent(
                task.getId(),
                task.getTitle(),
                TaskEventType.TASK_CREATED,
                LocalDateTime.now());

        taskEventClient.send(task.getId(), event);

        log.info("Task created event sent to topic: {}", task.getId());
    }

    public void publishTaskCompleted(Task task) {
        TaskEvent event = new TaskEvent(
                task.getId(),
                task.getTitle(),
                TaskEventType.TASK_COMPLETED,
                LocalDateTime.now()
        );

        taskEventClient.send(task.getId(), event);

        log.info("Task completed event sent to topic: {}", task.getId());
    }

    public void publishTaskFailed(Task task) {
        TaskEvent event = new TaskEvent(
                task.getId(),
                task.getTitle(),
                TaskEventType.TASK_FAILED,
                LocalDateTime.now()
        );

        taskEventClient.send(task.getId(), event);

        log.info("Task failed event sent to topic: {}", task.getId());
    }

}
