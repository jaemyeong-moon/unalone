package com.project.api.dto.care;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record VolunteerRequest(
        @NotBlank(message = "활동 가능 요일은 필수입니다")
        String availableDays,

        @NotNull(message = "활동 가능 시작 시간은 필수입니다")
        LocalTime availableTimeStart,

        @NotNull(message = "활동 가능 종료 시간은 필수입니다")
        LocalTime availableTimeEnd,

        Double radius,

        @NotNull(message = "위도는 필수입니다")
        Double latitude,

        @NotNull(message = "경도는 필수입니다")
        Double longitude,

        String introduction
) {
}
