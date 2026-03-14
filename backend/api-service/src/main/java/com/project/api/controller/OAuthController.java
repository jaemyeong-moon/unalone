package com.project.api.controller;

import com.project.api.config.OAuthProviderConfig;
import com.project.api.dto.auth.OAuthConnectionResponse;
import com.project.api.dto.auth.OAuthLinkRequest;
import com.project.api.dto.auth.OAuthLinkResponse;
import com.project.api.dto.auth.OAuthLoginRequest;
import com.project.api.dto.auth.OAuthLoginResponse;
import com.project.api.service.OAuthService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;
    private final OAuthProviderConfig oAuthProviderConfig;

    /**
     * OAuth 직접 로그인 - 프론트엔드에서 인가 코드를 직접 전달
     * POST /api/auth/oauth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<OAuthService.LoginResult>> oauthLogin(
            @Valid @RequestBody OAuthLoginRequest request) {

        OAuthService.LoginResult result = oAuthService.login(request);
        String message = result.isNewUser() ? "소셜 회원가입 및 로그인 성공" : "소셜 로그인 성공";
        return ResponseEntity.ok(ApiResponse.ok(result, message));
    }

    /**
     * OAuth 로그인 시작 - 제공자 인증 페이지 URL 반환
     * GET /api/auth/oauth/{provider}
     */
    @GetMapping("/{provider}")
    public ResponseEntity<ApiResponse<OAuthLoginResponse>> getAuthorizationUrl(
            @PathVariable String provider,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri) {

        String authorizationUrl = oAuthService.getAuthorizationUrl(provider, redirectUri);
        OAuthLoginResponse response = OAuthLoginResponse.of(authorizationUrl);
        return ResponseEntity.ok(ApiResponse.ok(response, "OAuth 인증 URL 생성 성공"));
    }

    /**
     * OAuth 콜백 처리 - Authorization Code를 JWT로 교환
     * GET /api/auth/oauth/{provider}/callback
     */
    @GetMapping("/{provider}/callback")
    public ResponseEntity<Void> handleCallback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String state) {

        OAuthService.CallbackResult result = oAuthService.handleCallback(provider, code, state);

        String frontendBaseUrl = oAuthProviderConfig.getFrontendRedirectUri();

        if (result.isEmailConflict()) {
            // 이메일 중복 - 로그인 페이지로 리다이렉트 (에러 정보 포함)
            String redirectUrl = frontendBaseUrl + "/login?error=email_exists&email="
                    + URLEncoder.encode(result.existingEmail(), StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }

        // 성공 - 프론트엔드 콜백 페이지로 리다이렉트 (JWT 포함)
        String redirectUrl = frontendBaseUrl + "/oauth/callback?token="
                + result.loginResponse().token()
                + "&isNewUser=" + result.isNewUser();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    /**
     * 기존 계정에 소셜 계정 연동
     * POST /api/auth/oauth/link
     */
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<OAuthLinkResponse>> linkOAuthAccount(
            @Valid @RequestBody OAuthLinkRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        OAuthLinkResponse response = oAuthService.linkOAuthAccount(
                userId, request.provider(), request.authorizationCode(), request.state());

        return ResponseEntity.ok(ApiResponse.ok(response, "소셜 계정 연동 성공"));
    }

    /**
     * 소셜 계정 연동 해제
     * DELETE /api/auth/oauth/link/{provider}
     */
    @DeleteMapping("/link/{provider}")
    public ResponseEntity<ApiResponse<Void>> unlinkOAuthAccount(
            @PathVariable String provider,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        oAuthService.unlinkOAuthAccount(userId, provider);

        return ResponseEntity.ok(ApiResponse.ok(null, "소셜 계정 연동이 해제되었습니다"));
    }

    /**
     * 연동된 OAuth 제공자 목록 조회
     * GET /api/auth/oauth/connections
     */
    @GetMapping("/connections")
    public ResponseEntity<ApiResponse<List<OAuthConnectionResponse>>> getConnectedProviders(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        List<OAuthConnectionResponse> connections = oAuthService.getConnectedProviders(userId);

        return ResponseEntity.ok(ApiResponse.ok(connections, "소셜 계정 연동 목록 조회 성공"));
    }
}
