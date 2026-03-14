package com.project.api.dto.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.api.domain.HealthJournal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record HealthJournalResponse(
        Long id,
        Long userId,
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
        String notes,
        Integer healthScore,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static HealthJournalResponse from(HealthJournal journal) {
        List<String> symptomList = journal.getSymptoms() != null && !journal.getSymptoms().isBlank()
                ? Arrays.asList(journal.getSymptoms().split(","))
                : List.of();
        List<String> medicationList = journal.getMedications() != null && !journal.getMedications().isBlank()
                ? Arrays.asList(journal.getMedications().split(","))
                : List.of();

        return new HealthJournalResponse(
                journal.getId(),
                journal.getUser().getId(),
                journal.getDate(),
                journal.getMoodScore(),
                journal.getSleepHours(),
                journal.getSleepQuality(),
                journal.getMealCount(),
                journal.getMealQuality(),
                journal.getExerciseMinutes(),
                journal.getExerciseType(),
                symptomList,
                medicationList,
                journal.getPainLevel(),
                journal.getNotes(),
                journal.getHealthScore(),
                journal.getCreatedAt(),
                journal.getUpdatedAt()
        );
    }
}
