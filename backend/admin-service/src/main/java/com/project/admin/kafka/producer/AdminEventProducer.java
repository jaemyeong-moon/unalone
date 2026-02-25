package com.project.admin.kafka.producer;

import com.project.common.event.DomainEvent;
import com.project.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminEventProducer {
    private final EventPublisher eventPublisher;

    public void publishEvent(String topic, DomainEvent event) {
        log.info("Publishing event [{}] to topic [{}]", event.getEventType(), topic);
        eventPublisher.publish(topic, event);
    }
}
