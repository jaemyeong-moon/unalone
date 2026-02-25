package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class CheckInCompletedEvent extends DomainEvent {

    private final Long userId;
    private final String message;
    private final LocalDateTime checkedAt;

    public CheckInCompletedEvent(Long userId, String message) {
        super("CHECKIN_COMPLETED", String.valueOf(userId), "api-service");
        this.userId = userId;
        this.message = message;
        this.checkedAt = LocalDateTime.now();
    }
}
