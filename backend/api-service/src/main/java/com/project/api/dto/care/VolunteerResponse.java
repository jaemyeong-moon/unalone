package com.project.api.dto.care;

import com.project.api.domain.Volunteer;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record VolunteerResponse(
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
    public static VolunteerResponse from(Volunteer volunteer) {
        return new VolunteerResponse(
                volunteer.getId(),
                volunteer.getUserId(),
                volunteer.getAvailableDays(),
                volunteer.getAvailableTimeStart(),
                volunteer.getAvailableTimeEnd(),
                volunteer.getRadius(),
                volunteer.getLatitude(),
                volunteer.getLongitude(),
                volunteer.getIntroduction(),
                volunteer.getStatus().name(),
                volunteer.getTrustScore(),
                volunteer.getTotalVisits(),
                volunteer.getApprovedAt(),
                volunteer.getCreatedAt()
        );
    }
}
