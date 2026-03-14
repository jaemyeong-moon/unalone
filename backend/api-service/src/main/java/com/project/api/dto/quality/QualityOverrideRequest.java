package com.project.api.dto.quality;

import jakarta.validation.constraints.NotBlank;

public record QualityOverrideRequest(
        @NotBlank(message = "품질 등급은 필수입니다")
        String grade
) {
}
