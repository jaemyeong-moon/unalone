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

    private static final String ADMIN_EVENTS_TOPIC = "admin-events";

    private final EventPublisher eventPublisher;

    public void publishAdminEvent(DomainEvent event) {
        log.info("Publishing admin event: {}", event);
        eventPublisher.publish(ADMIN_EVENTS_TOPIC, event);
    }
}
