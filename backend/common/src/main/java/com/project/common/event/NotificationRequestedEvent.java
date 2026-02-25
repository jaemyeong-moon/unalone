package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@ToString(callSuper = true)
public class NotificationRequestedEvent extends DomainEvent {

    private final Long userId;
    private final String type;
    private final String title;
    private final String message;
    private final Map<String, String> metadata;
    private final LocalDateTime requestedAt;

    public NotificationRequestedEvent(Long userId, String type, String title, String message, Map<String, String> metadata) {
        super("NOTIFICATION_REQUESTED", String.valueOf(userId), "event-service");
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.metadata = metadata;
        this.requestedAt = LocalDateTime.now();
    }
}
