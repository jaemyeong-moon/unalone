package com.project.event.kafka.consumer;

import com.project.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 건강 일지 이벤트 컨슈머.
 * health-journal-events, health-alert-events 토픽을 구독하여 MongoDB에 로깅합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthEventConsumer {

    private final EventHandler eventHandler;

    @KafkaListener(
            topics = {"${kafka.topics.health-journal-events:health-journal-events}", "${kafka.topics.health-alert-events:health-alert-events}"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.debug("Health event received: {}", message);
        try {
            eventHandler.handleEvent(message);
        } catch (Exception e) {
            log.error("Failed to consume health event: {}", e.getMessage(), e);
        }
    }
}
