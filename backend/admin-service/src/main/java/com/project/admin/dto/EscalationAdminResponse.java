package com.project.admin.dto;

import com.project.admin.domain.Escalation;

import java.time.LocalDateTime;

public record EscalationAdminResponse(
        Long id,
        Long userId,
        String userName,
        String userPhone,
        String stage,
        String status,
        LocalDateTime triggeredAt,
        LocalDateTime resolvedAt,
        boolean resolved,
        String resolvedBy,
        int notificationsSent,
        String notes,
        String notifiedContacts
) {
    public static EscalationAdminResponse from(Escalation escalation) {
        return new EscalationAdminResponse(
                escalation.getId(),
                escalation.getUser().getId(),
                escalation.getUser().getName(),
                escalation.getUser().getPhone(),
                escalation.getStage(),
                escalation.getStatus(),
                escalation.getTriggeredAt(),
                escalation.getResolvedAt(),
                escalation.isResolved(),
                escalation.getResolvedBy(),
                escalation.getNotificationsSent(),
                escalation.getNotes(),
                escalation.getNotifiedContacts()
        );
    }
}
