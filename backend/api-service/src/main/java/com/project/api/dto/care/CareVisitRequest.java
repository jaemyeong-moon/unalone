package com.project.api.dto.care;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CareVisitRequest(
        @NotNull(message = "매칭 ID는 필수입니다")
        Long careMatchId,

        @NotNull(message = "방문 예정 날짜는 필수입니다")
        LocalDate scheduledDate,

        @NotNull(message = "방문 예정 시간은 필수입니다")
        LocalTime scheduledTime
) {
}
