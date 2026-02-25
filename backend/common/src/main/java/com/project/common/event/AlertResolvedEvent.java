package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class AlertResolvedEvent extends DomainEvent {

    private final String alertId;
    private final Long resolvedBy;
    private final LocalDateTime resolvedAt;

    public AlertResolvedEvent(String alertId, Long resolvedBy) {
        super("ALERT_RESOLVED", alertId, "admin-service");
        this.alertId = alertId;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
    }
}
