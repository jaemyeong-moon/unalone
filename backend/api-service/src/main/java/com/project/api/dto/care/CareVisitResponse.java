package com.project.api.dto.care;

import com.project.api.domain.CareVisit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CareVisitResponse(
        Long id,
        Long careMatchId,
        Long volunteerId,
        Long receiverId,
        LocalDate scheduledDate,
        LocalTime scheduledTime,
        String status,
        String reportContent,
        String receiverCondition,
        String specialNotes,
        LocalDateTime visitedAt,
        LocalDateTime createdAt
) {
    public static CareVisitResponse from(CareVisit visit) {
        return new CareVisitResponse(
                visit.getId(),
                visit.getCareMatchId(),
                visit.getVolunteerId(),
                visit.getReceiverId(),
                visit.getScheduledDate(),
                visit.getScheduledTime(),
                visit.getStatus().name(),
                visit.getReportContent(),
                visit.getReceiverCondition() != null ? visit.getReceiverCondition().name() : null,
                visit.getSpecialNotes(),
                visit.getVisitedAt(),
                visit.getCreatedAt()
        );
    }
}
