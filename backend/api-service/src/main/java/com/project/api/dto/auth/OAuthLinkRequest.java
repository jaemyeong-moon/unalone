package com.project.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthLinkRequest(
        @NotBlank(message = "OAuth 제공자는 필수입니다")
        String provider,

        @NotBlank(message = "인가 코드는 필수입니다")
        String authorizationCode,

        @NotBlank(message = "state 토큰은 필수입니다")
        String state
) {
}
