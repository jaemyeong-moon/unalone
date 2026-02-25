package com.project.api.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"${kafka.topics.alert-events:alert-events}"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.info("Received event: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.path("eventType").asText();

            switch (eventType) {
                case "ALERT_CREATED" -> handleAlertCreated(jsonNode);
                case "ALERT_RESOLVED" -> handleAlertResolved(jsonNode);
                default -> log.debug("Unhandled event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
        }
    }

    private void handleAlertCreated(JsonNode jsonNode) {
        Long userId = jsonNode.path("userId").asLong();
        String level = jsonNode.path("level").asText();
        log.warn("Alert created for user {}: level={}", userId, level);
    }

    private void handleAlertResolved(JsonNode jsonNode) {
        String alertId = jsonNode.path("alertId").asText();
        log.info("Alert resolved: {}", alertId);
    }
}
