package com.project.event.kafka.consumer;

import com.project.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 돌봄 매칭 이벤트 컨슈머.
 * care-match-events 토픽을 구독하여 MongoDB에 로깅합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CareMatchEventConsumer {

    private final EventHandler eventHandler;

    @KafkaListener(
            topics = {"${kafka.topics.care-match-events:care-match-events}"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.debug("Care match event received: {}", message);
        try {
            eventHandler.handleEvent(message);
        } catch (Exception e) {
            log.error("Failed to consume care match event: {}", e.getMessage(), e);
        }
    }
}
