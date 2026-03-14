package com.project.api.dto.health;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record HealthSummaryResponse(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        Integer moodScore,
        Integer healthScore,
        Double weeklyMoodAverage,
        Double weeklyHealthScoreAverage,
        long totalEntries
) {
}
