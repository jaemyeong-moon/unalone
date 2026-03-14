package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class EscalationTriggeredEvent extends DomainEvent {

    private final Long userId;
    private final Long escalationId;
    private final String previousLevel;
    private final String currentLevel;
    private final LocalDateTime triggeredAt;

    public EscalationTriggeredEvent(Long userId, Long escalationId, String previousLevel, String currentLevel) {
        super("ESCALATION_LEVEL_CHANGED", String.valueOf(userId), "api-service");
        this.userId = userId;
        this.escalationId = escalationId;
        this.previousLevel = previousLevel;
        this.currentLevel = currentLevel;
        this.triggeredAt = LocalDateTime.now();
    }
}
