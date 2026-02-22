package com.project.admin.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminEventConsumer {

    @KafkaListener(
        topics = "${kafka.topics.admin-events:admin-events}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.info("Received admin event: {}", message);
        // TODO: Implement event processing logic
    }
}
