package hu.backend.kafka;

import hu.backend.event.TaskEvent;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient
public interface TaskEventClient {

    @Topic("task-events")
    void send(@KafkaKey Long taskId, TaskEvent event);
}
