package com.project.admin.dto;

import com.project.admin.domain.Alert;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
public record AlertResponse(
        String id,
        Long userId,
        String userName,
        String level,
        String message,
        String status,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
    public static AlertResponse from(Alert alert, String userName) {
        return new AlertResponse(
                alert.getId(),
                alert.getUserId(),
                userName,
                alert.getLevel(),
                alert.getMessage(),
                alert.getStatus(),
                alert.getCreatedAt(),
                alert.getResolvedAt()
        );
    }
}
