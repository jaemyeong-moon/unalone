package com.project.admin.dto;

import com.project.admin.domain.CareVisit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CareVisitAdminResponse(
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
    public static CareVisitAdminResponse from(CareVisit visit) {
        return new CareVisitAdminResponse(
                visit.getId(),
                visit.getCareMatchId(),
                visit.getVolunteerId(),
                visit.getReceiverId(),
                visit.getScheduledDate(),
                visit.getScheduledTime(),
                visit.getStatus(),
                visit.getReportContent(),
                visit.getReceiverCondition(),
                visit.getSpecialNotes(),
                visit.getVisitedAt(),
                visit.getCreatedAt()
        );
    }
}
