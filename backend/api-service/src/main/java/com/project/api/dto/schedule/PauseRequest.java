package com.project.api.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record PauseRequest(
        String reason,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate pauseEndDate
) {
}
