package com.project.api.dto.health;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record MoodTrendItem(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        Double score
) {
}
