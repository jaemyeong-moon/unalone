package com.project.event.kafka.consumer;

import com.project.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventConsumer {

    private final EventHandler eventHandler;

    @KafkaListener(topics = {"${kafka.topics.api-events}", "${kafka.topics.admin-events}"},
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        log.info("Received event: {}", message);
        eventHandler.handleEvent(message);
    }
}
