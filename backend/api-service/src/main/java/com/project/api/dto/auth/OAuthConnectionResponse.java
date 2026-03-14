package com.project.api.dto.auth;

import com.project.api.domain.UserOAuthConnection;

import java.time.LocalDateTime;

public record OAuthConnectionResponse(
        String provider,
        String email,
        String nickname,
        String profileImageUrl,
        LocalDateTime connectedAt
) {
    public static OAuthConnectionResponse from(UserOAuthConnection connection) {
        return new OAuthConnectionResponse(
                connection.getOauthProvider(),
                connection.getOauthEmail(),
                connection.getOauthNickname(),
                connection.getProfileImageUrl(),
                connection.getConnectedAt()
        );
    }
}
