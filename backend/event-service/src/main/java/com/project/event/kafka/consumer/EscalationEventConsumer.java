package com.project.event.kafka.consumer;

import com.project.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 에스컬레이션 이벤트 컨슈머.
 * escalation-events, anomaly-events 토픽을 구독하여 MongoDB에 로깅합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EscalationEventConsumer {

    private final EventHandler eventHandler;

    @KafkaListener(
            topics = {"${kafka.topics.escalation-events:escalation-events}", "${kafka.topics.anomaly-events:anomaly-events}"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.debug("Escalation/Anomaly event received: {}", message);
        try {
            eventHandler.handleEvent(message);
        } catch (Exception e) {
            log.error("Failed to consume escalation/anomaly event: {}", e.getMessage(), e);
        }
    }
}
