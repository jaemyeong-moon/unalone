package com.project.api.dto.guardian;

import jakarta.validation.constraints.NotBlank;

public record GuardianRequest(
        @NotBlank(message = "보호자 이름은 필수입니다")
        String name,

        @NotBlank(message = "보호자 연락처는 필수입니다")
        String phone,

        String relationship
) {
}
