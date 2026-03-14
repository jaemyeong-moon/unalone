package com.project.api.dto.notification;

import com.project.api.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        Long relatedId,
        String relatedType,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedId(),
                notification.getRelatedType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
