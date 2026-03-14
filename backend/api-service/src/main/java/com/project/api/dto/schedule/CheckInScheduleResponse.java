package com.project.api.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.api.domain.CheckInSchedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public record CheckInScheduleResponse(
        Long id,
        Long userId,
        Integer intervalHours,
        @JsonFormat(pattern = "HH:mm")
        LocalTime preferredTime,
        List<String> activeDays,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate pauseUntil,
        boolean paused,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime nextCheckInDue,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static CheckInScheduleResponse from(CheckInSchedule schedule) {
        List<String> days = schedule.getActiveDays() != null
                ? Arrays.asList(schedule.getActiveDays().split(","))
                : List.of();
        return new CheckInScheduleResponse(
                schedule.getId(),
                schedule.getUser().getId(),
                schedule.getIntervalHours(),
                schedule.getPreferredTime(),
                days,
                schedule.getPauseUntil(),
                schedule.isPaused(),
                schedule.getNextCheckInDue(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
