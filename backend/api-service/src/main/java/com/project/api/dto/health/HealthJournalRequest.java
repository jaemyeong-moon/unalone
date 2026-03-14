package com.project.api.dto.health;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HealthJournalRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        Integer moodScore,
        BigDecimal sleepHours,
        Integer sleepQuality,
        Integer mealCount,
        Integer mealQuality,
        Integer exerciseMinutes,
        String exerciseType,
        List<String> symptoms,
        List<String> medications,
        Integer painLevel,
        String notes
) {
}
