package hu.backend.kafka;

import hu.backend.event.TaskEvent;
import hu.backend.event.TaskEventType;
import hu.backend.service.RxJavaTaskProcessor;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@KafkaListener(
        groupId = "task-manager",
        offsetReset = OffsetReset.EARLIEST
)
public class TaskEventConsumer {

    private final RxJavaTaskProcessor taskProcessor;

    public TaskEventConsumer(RxJavaTaskProcessor taskProcessor) {
        this.taskProcessor = taskProcessor;
    }

    @Topic("task-events")
    public void receive(@KafkaKey Long taskId, TaskEvent event) {
        log.info("Received Kafka event. key={}, event={}", taskId, event);

        if(event.type() != TaskEventType.TASK_CREATED) {
            log.info("Ignoring event type: {}", event.type());
            return;
        }

        taskProcessor.process(event)
                .subscribe(
                        task -> log.info("RxJava consumer finished task id={}", task.getId()),
                        error -> log.error("RxJava consumer failed to process event. taskId={}", event.taskId(), error)
                );
    }

}
