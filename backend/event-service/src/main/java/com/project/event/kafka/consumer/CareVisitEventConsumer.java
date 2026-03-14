package com.project.event.kafka.consumer;

import com.project.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 돌봄 방문 이벤트 컨슈머.
 * care-visit-events 토픽을 구독하여 MongoDB에 로깅합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CareVisitEventConsumer {

    private final EventHandler eventHandler;

    @KafkaListener(
            topics = {"${kafka.topics.care-visit-events:care-visit-events}"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.debug("Care visit event received: {}", message);
        try {
            eventHandler.handleEvent(message);
        } catch (Exception e) {
            log.error("Failed to consume care visit event: {}", e.getMessage(), e);
        }
    }
}
