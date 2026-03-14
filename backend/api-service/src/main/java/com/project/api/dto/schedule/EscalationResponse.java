package com.project.api.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.api.domain.Escalation;

import java.time.LocalDateTime;

public record EscalationResponse(
        Long id,
        Long userId,
        String userName,
        String stage,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime triggeredAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime resolvedAt,
        boolean resolved,
        String notifiedContacts,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
    public static EscalationResponse from(Escalation escalation) {
        return new EscalationResponse(
                escalation.getId(),
                escalation.getUser().getId(),
                escalation.getUser().getName(),
                escalation.getStage().name(),
                escalation.getTriggeredAt(),
                escalation.getResolvedAt(),
                escalation.isResolved(),
                escalation.getNotifiedContacts(),
                escalation.getCreatedAt()
        );
    }
}
