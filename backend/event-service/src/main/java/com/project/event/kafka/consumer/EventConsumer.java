package com.project.event.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.event.handler.AlertHandler;
import com.project.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
        log.info("Event service received: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.path("eventType").asText();

            // Store all events
            eventHandler.handleEvent(message);

            switch (eventType) {
                case "CHECKIN_COMPLETED" -> log.info("CheckIn completed: userId={}", jsonNode.path("userId").asLong());
                case "CHECKIN_MISSED" -> {
                    Long userId = jsonNode.path("userId").asLong();
                    int missedCount = jsonNode.path("missedCount").asInt(1);
                    alertHandler.handleCheckInMissed(userId, missedCount);
                }
                case "ALERT_RESOLVED" -> {
                    String alertId = jsonNode.path("alertId").asText();
                    alertHandler.handleAlertResolved(alertId);
                }
                default -> log.debug("Event stored: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
        }
    }
}
