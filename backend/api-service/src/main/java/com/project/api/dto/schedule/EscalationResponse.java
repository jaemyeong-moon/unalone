package com.project.api.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.api.domain.Escalation;

import java.time.LocalDateTime;

public record EscalationResponse(
        Long id,
        Long userId,
        String userName,
        String userPhone,
        String stage,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime triggeredAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime resolvedAt,
        boolean resolved,
        String resolvedBy,
        int notificationsSent,
        String notes,
        String notifiedContacts,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
    public static EscalationResponse from(Escalation escalation) {
        return new EscalationResponse(
                escalation.getId(),
                escalation.getUser().getId(),
                escalation.getUser().getName(),
                escalation.getUser().getPhone(),
                escalation.getStage().name(),
                escalation.getStatus().name(),
                escalation.getTriggeredAt(),
                escalation.getResolvedAt(),
                escalation.isResolved(),
                escalation.getResolvedBy(),
                escalation.getNotificationsSent(),
                escalation.getNotes(),
                escalation.getNotifiedContacts(),
                escalation.getCreatedAt()
        );
    }
}
