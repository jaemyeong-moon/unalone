package com.project.api.dto.health;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record HealthTrendResponse(
        String period,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,
        long totalEntries,
        TrendData moodTrend,
        TrendData healthScoreTrend,
        TrendData sleepTrend,
        TrendData mealTrend,
        ExerciseFrequency exerciseFrequency
) {

    public record TrendData(
            List<MoodTrendItem> data,
            Double average,
            String trend,
            Double changePercent
    ) {
    }

    public record ExerciseFrequency(
            long totalDays,
            long activeDays,
            Double rate
    ) {
    }
}
