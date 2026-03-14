package com.project.api.service;

import com.project.api.dto.auth.OAuthUserInfo;

/**
 * OAuth 제공자별 클라이언트 인터페이스
 */
public interface OAuthProviderClient {

    /**
     * OAuth 인증 URL 생성
     */
    String buildAuthorizationUrl(String state);

    /**
     * Authorization Code -> Access Token 교환
     */
    String exchangeToken(String code);

    /**
     * Access Token으로 사용자 정보 조회
     */
    OAuthUserInfo getUserInfo(String accessToken);

    /**
     * 지원하는 OAuth 제공자명 반환
     */
    String getProviderName();
}
