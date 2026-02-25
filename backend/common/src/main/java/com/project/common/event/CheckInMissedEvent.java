package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class CheckInMissedEvent extends DomainEvent {

    private final Long userId;
    private final LocalDateTime expectedAt;
    private final int missedCount;

    public CheckInMissedEvent(Long userId, LocalDateTime expectedAt, int missedCount) {
        super("CHECKIN_MISSED", String.valueOf(userId), "api-service");
        this.userId = userId;
        this.expectedAt = expectedAt;
        this.missedCount = missedCount;
    }
}
