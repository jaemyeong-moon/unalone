package com.project.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record QualityOverrideRequest(
        @NotBlank(message = "품질 등급은 필수입니다")
        String grade
) {
}
