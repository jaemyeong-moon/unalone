package com.project.common.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String topic, DomainEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getAggregateId(), message);
            log.info("Published event [{}] to topic [{}]: {}", event.getEventType(), topic, event.getEventId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event [{}]: {}", event.getEventId(), e.getMessage(), e);
            throw new RuntimeException("Failed to serialize domain event", e);
        }
    }
}
