package com.project.api.dto.auth;

public record OAuthLoginResponse(
        String authorizationUrl
) {
    public static OAuthLoginResponse of(String authorizationUrl) {
        return new OAuthLoginResponse(authorizationUrl);
    }
}
