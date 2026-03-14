package com.project.api.dto.auth;

/**
 * OAuth 제공자로부터 받은 사용자 정보를 담는 DTO
 */
public record OAuthUserInfo(
        String oauthId,
        String email,
        String name,
        String profileImageUrl
) {
}
