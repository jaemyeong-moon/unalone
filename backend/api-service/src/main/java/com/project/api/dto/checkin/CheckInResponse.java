package com.project.api.dto.checkin;

import com.project.api.domain.CheckIn;

import java.time.LocalDateTime;

public record CheckInResponse(
        Long id,
        Long userId,
        String userName,
        String status,
        String message,
        Integer moodScore,
        LocalDateTime checkedAt
) {
    public static CheckInResponse from(CheckIn checkIn) {
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getUser().getId(),
                checkIn.getUser().getName(),
                checkIn.getStatus().name(),
                checkIn.getMessage(),
                checkIn.getMoodScore(),
                checkIn.getCheckedAt()
        );
    }
}
