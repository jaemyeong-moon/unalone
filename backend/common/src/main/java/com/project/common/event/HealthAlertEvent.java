package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class HealthAlertEvent extends DomainEvent {

    private final Long userId;
    private final String alertType;
    private final String severity;
    private final String description;
    private final LocalDateTime detectedAt;

    public HealthAlertEvent(Long userId, String alertType, String severity, String description) {
        super("HEALTH_ALERT_CREATED", String.valueOf(userId), "api-service");
        this.userId = userId;
        this.alertType = alertType;
        this.severity = severity;
        this.description = description;
        this.detectedAt = LocalDateTime.now();
    }
}
