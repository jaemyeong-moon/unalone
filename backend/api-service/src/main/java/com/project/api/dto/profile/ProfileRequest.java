package com.project.api.dto.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProfileRequest(
        @Min(value = 1, message = "체크 주기는 최소 1시간입니다")
        @Max(value = 72, message = "체크 주기는 최대 72시간입니다")
        Integer checkIntervalHours,

        String activeHoursStart,
        String activeHoursEnd,
        String address,
        String emergencyNote
) {
}
