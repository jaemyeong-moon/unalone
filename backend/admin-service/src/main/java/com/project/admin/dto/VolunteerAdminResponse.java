package com.project.admin.dto;

import com.project.admin.domain.Volunteer;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record VolunteerAdminResponse(
        Long id,
        Long userId,
        String availableDays,
        LocalTime availableTimeStart,
        LocalTime availableTimeEnd,
        Double radius,
        Double latitude,
        Double longitude,
        String introduction,
        String status,
        Integer trustScore,
        Integer totalVisits,
        LocalDateTime approvedAt,
        LocalDateTime createdAt
) {
    public static VolunteerAdminResponse from(Volunteer volunteer) {
        return new VolunteerAdminResponse(
                volunteer.getId(),
                volunteer.getUserId(),
                volunteer.getAvailableDays(),
                volunteer.getAvailableTimeStart(),
                volunteer.getAvailableTimeEnd(),
                volunteer.getRadius(),
                volunteer.getLatitude(),
                volunteer.getLongitude(),
                volunteer.getIntroduction(),
                volunteer.getStatus(),
                volunteer.getTrustScore(),
                volunteer.getTotalVisits(),
                volunteer.getApprovedAt(),
                volunteer.getCreatedAt()
        );
    }
}
