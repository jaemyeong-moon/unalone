package com.project.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 상품 등록/수정 요청 DTO
 */
public record ProductRequest(
        @NotBlank(message = "상품명은 필수입니다")
        String name,

        @NotNull(message = "가격은 필수입니다")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다")
        BigDecimal price,

        @Min(value = 0, message = "재고는 0 이상이어야 합니다")
        int stock,

        String category,

        String description
) {
}
