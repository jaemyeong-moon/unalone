package com.project.api.service;

import com.project.api.config.OAuthProviderConfig;
import com.project.api.dto.auth.OAuthUserInfo;
import com.project.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthProviderClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final OAuthProviderConfig oAuthProviderConfig;
    private final WebClient.Builder webClientBuilder;

    /**
     * 카카오 인증 URL 생성
     */
    @Override
    public String buildAuthorizationUrl(String state) {
        OAuthProviderConfig.ProviderProperties props = oAuthProviderConfig.getKakao();
        return props.getAuthorizationUri()
                + "?client_id=" + props.getClientId()
                + "&redirect_uri=" + props.getRedirectUri()
                + "&response_type=code"
                + "&state=" + state
                + "&scope=" + props.getScope();
    }

    /**
     * Authorization Code -> Access Token 교환
     */
    @Override
    public String exchangeToken(String code) {
        OAuthProviderConfig.ProviderProperties props = oAuthProviderConfig.getKakao();

        try {
            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(props.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("grant_type=authorization_code"
                            + "&client_id=" + props.getClientId()
                            + "&client_secret=" + props.getClientSecret()
                            + "&redirect_uri=" + props.getRedirectUri()
                            + "&code=" + code)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(TIMEOUT);

            if (response == null || !response.containsKey("access_token")) {
                throw BusinessException.badRequest("카카오 토큰 교환에 실패했습니다");
            }

            return (String) response.get("access_token");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 토큰 교환 중 오류 발생", e);
            throw BusinessException.badRequest("소셜 로그인 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * Access Token으로 사용자 정보 조회
     */
    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        OAuthProviderConfig.ProviderProperties props = oAuthProviderConfig.getKakao();

        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(props.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(TIMEOUT);

            if (response == null) {
                throw BusinessException.badRequest("카카오 사용자 정보 조회에 실패했습니다");
            }

            String oauthId = String.valueOf(response.get("id"));

            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

            Map<String, Object> profile = kakaoAccount != null
                    ? (Map<String, Object>) kakaoAccount.get("profile") : null;
            String nickname = profile != null ? (String) profile.get("nickname") : null;
            String profileImage = profile != null ? (String) profile.get("profile_image_url") : null;

            return new OAuthUserInfo(oauthId, email, nickname, profileImage);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 오류 발생", e);
            throw BusinessException.badRequest("소셜 로그인 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    @Override
    public String getProviderName() {
        return "kakao";
    }
}
