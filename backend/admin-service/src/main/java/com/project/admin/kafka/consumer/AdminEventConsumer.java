package com.project.admin.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminEventConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"${kafka.topics.checkin-events:checkin-events}", "${kafka.topics.alert-events:alert-events}"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.info("Admin received event: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.path("eventType").asText();
            switch (eventType) {
                case "CHECKIN_MISSED" -> log.warn("CheckIn missed: userId={}", jsonNode.path("userId").asLong());
                case "ALERT_CREATED" -> log.warn("Alert created: alertId={}, level={}", jsonNode.path("alertId").asText(), jsonNode.path("level").asText());
                default -> log.debug("Unhandled event: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
        }
    }
}
