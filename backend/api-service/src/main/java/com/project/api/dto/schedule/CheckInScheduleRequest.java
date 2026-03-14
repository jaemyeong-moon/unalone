package com.project.api.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CheckInScheduleRequest(
        Integer intervalHours,
        @JsonFormat(pattern = "HH:mm")
        LocalTime preferredTime,
        List<String> activeDays,
        @JsonFormat(pattern = "HH:mm")
        LocalTime quietStartTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime quietEndTime,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate pauseUntil
) {
}
