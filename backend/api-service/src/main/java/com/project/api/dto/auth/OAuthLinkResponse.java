package com.project.api.dto.auth;

import java.time.LocalDateTime;

public record OAuthLinkResponse(
        String provider,
        LocalDateTime linkedAt
) {
    public static OAuthLinkResponse of(String provider, LocalDateTime linkedAt) {
        return new OAuthLinkResponse(provider, linkedAt);
    }
}
