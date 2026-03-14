package com.project.api.dto.care;

import jakarta.validation.constraints.NotNull;

public record CareMatchRequest(
        @NotNull(message = "수신자 ID는 필수입니다")
        Long receiverId
) {
}
