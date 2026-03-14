package com.project.event.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.event.handler.AlertHandler;
import com.project.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 이벤트 컨슈머.
 * checkin-events, alert-events 토픽을 구독하여 이벤트를 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventHandler eventHandler;
    private final AlertHandler alertHandler;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"${kafka.topics.checkin-events:checkin-events}", "${kafka.topics.alert-events:alert-events}"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.debug("Event received: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText();

            // 모든 이벤트를 MongoDB에 로깅
            eventHandler.handleEvent(message);

            // 이벤트 타입별 추가 처리
            dispatchEvent(eventType, root);

        } catch (Exception e) {
            log.error("Failed to consume event: {}", e.getMessage(), e);
        }
    }

    /**
     * 이벤트 타입에 따라 적절한 핸들러로 디스패치합니다.
     */
    private void dispatchEvent(String eventType, JsonNode root) {
        switch (eventType) {
            case "CHECKIN_COMPLETED" ->
                    log.info("CheckIn completed: userId={}", root.path("userId").asLong());

            case "CHECKIN_MISSED" -> {
                long userId = root.path("userId").asLong();
                int missedCount = root.path("missedCount").asInt(1);
                alertHandler.handleCheckInMissed(userId, missedCount);
            }

            case "ALERT_RESOLVED" -> {
                String alertId = root.path("alertId").asText();
                Long resolvedBy = root.path("resolvedBy").isMissingNode() ? null : root.path("resolvedBy").asLong();
                alertHandler.handleAlertResolved(alertId, resolvedBy);
            }

            default -> log.debug("Event stored without additional processing: type={}", eventType);
        }
    }
}
