package com.project.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderStatusRequest {

    @NotBlank(message = "변경할 상태는 필수입니다")
    private String status;
}
