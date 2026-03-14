package com.project.admin.dto;

import com.project.admin.domain.Escalation;

import java.time.LocalDateTime;

public record EscalationAdminResponse(
        Long id,
        Long userId,
        String userName,
        String stage,
        LocalDateTime triggeredAt,
        LocalDateTime resolvedAt,
        boolean resolved,
        String notifiedContacts
) {
    public static EscalationAdminResponse from(Escalation escalation) {
        return new EscalationAdminResponse(
                escalation.getId(),
                escalation.getUser().getId(),
                escalation.getUser().getName(),
                escalation.getStage(),
                escalation.getTriggeredAt(),
                escalation.getResolvedAt(),
                escalation.isResolved(),
                escalation.getNotifiedContacts()
        );
    }
}
