package com.project.api.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiEventConsumer {

    @KafkaListener(
            topics = "${kafka.topics.api-events:api-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.info("Received message from api-events topic: {}", message);
    }
}
