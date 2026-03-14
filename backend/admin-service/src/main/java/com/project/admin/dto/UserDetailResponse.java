package com.project.admin.dto;

import com.project.admin.domain.User;

import java.time.LocalDateTime;

/**
 * 사용자 상세 정보 응답 DTO
 */
public record UserDetailResponse(
        Long id,
        String email,
        String name,
        String phone,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime lastCheckInAt
) {
    public static UserDetailResponse from(User user, LocalDateTime lastCheckInAt) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                lastCheckInAt
        );
    }
}
