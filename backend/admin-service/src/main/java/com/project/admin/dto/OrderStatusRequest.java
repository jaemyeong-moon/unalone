package com.project.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 주문 상태 변경 요청 DTO
 */
public record OrderStatusRequest(
        @NotBlank(message = "변경할 상태는 필수입니다")
        String status
) {
}
