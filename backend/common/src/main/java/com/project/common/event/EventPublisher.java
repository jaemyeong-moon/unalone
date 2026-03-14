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

    /**
     * 도메인 이벤트를 Kafka 토픽으로 발행합니다.
     *
     * @param topic 발행할 Kafka 토픽 이름
     * @param event 발행할 도메인 이벤트
     * @throws EventSerializationException 이벤트 직렬화 실패 시
     */
    public void publish(String topic, DomainEvent event) {
        try {
            var message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getAggregateId(), message);
            log.info("Published event [{}] to topic [{}]: {}", event.getEventType(), topic, event.getEventId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event [{}]: {}", event.getEventId(), e.getMessage(), e);
            throw new EventSerializationException("Failed to serialize domain event: " + event.getEventId(), e);
        }
    }
}
