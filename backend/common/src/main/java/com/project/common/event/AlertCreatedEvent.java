package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class AlertCreatedEvent extends DomainEvent {

    private final String alertId;
    private final Long userId;
    private final String level;
    private final String message;
    private final LocalDateTime createdAt;

    public AlertCreatedEvent(String alertId, Long userId, String level, String message) {
        super("ALERT_CREATED", alertId, "event-service");
        this.alertId = alertId;
        this.userId = userId;
        this.level = level;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
