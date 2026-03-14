package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 에스컬레이션 레벨 변경 또는 생성/해제 시 발행되는 이벤트.
 * escalation-events 토픽으로 발행됩니다.
 */
@Getter
@ToString(callSuper = true)
public class EscalationTriggeredEvent extends DomainEvent {

    private final Long userId;
    private final Long escalationId;
    private final String previousLevel;
    private final String currentLevel;
    private final LocalDateTime triggeredAt;

    public EscalationTriggeredEvent(Long userId, Long escalationId, String previousLevel, String currentLevel) {
        super(determineEventType(previousLevel, currentLevel), String.valueOf(userId), "api-service");
        this.userId = userId;
        this.escalationId = escalationId;
        this.previousLevel = previousLevel;
        this.currentLevel = currentLevel;
        this.triggeredAt = LocalDateTime.now();
    }

    private static String determineEventType(String previousLevel, String currentLevel) {
        if ("RESOLVED".equals(currentLevel)) {
            return "ESCALATION_RESOLVED";
        }
        if (previousLevel == null) {
            return "ESCALATION_CREATED";
        }
        return "ESCALATION_LEVEL_CHANGED";
    }
}
