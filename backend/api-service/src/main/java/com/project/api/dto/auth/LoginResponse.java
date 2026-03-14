package com.project.api.dto.auth;

import com.project.api.domain.User;

public record LoginResponse(
        String token,
        Long userId,
        String name,
        String email,
        String role
) {
    public static LoginResponse of(String token, User user) {
        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
