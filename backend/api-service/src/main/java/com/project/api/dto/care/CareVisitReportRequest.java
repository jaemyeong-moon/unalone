package com.project.api.dto.care;

import com.project.api.domain.enums.ReceiverCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CareVisitReportRequest(
        @NotBlank(message = "보고 내용은 필수입니다")
        String reportContent,

        @NotNull(message = "수신자 상태는 필수입니다")
        ReceiverCondition receiverCondition,

        String specialNotes
) {
}
