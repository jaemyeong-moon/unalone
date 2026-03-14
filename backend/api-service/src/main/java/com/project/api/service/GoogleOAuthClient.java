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
public class GoogleOAuthClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final OAuthProviderConfig oAuthProviderConfig;
    private final WebClient.Builder webClientBuilder;

    /**
     * 구글 인증 URL 생성
     */
    public String buildAuthorizationUrl(String state) {
        OAuthProviderConfig.ProviderProperties props = oAuthProviderConfig.getGoogle();
        return props.getAuthorizationUri()
                + "?client_id=" + props.getClientId()
                + "&redirect_uri=" + props.getRedirectUri()
                + "&response_type=code"
                + "&state=" + state
                + "&scope=" + props.getScope()
                + "&access_type=offline";
    }

    /**
     * Authorization Code -> Access Token 교환
     */
    public String exchangeToken(String code) {
        OAuthProviderConfig.ProviderProperties props = oAuthProviderConfig.getGoogle();

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
                throw BusinessException.badRequest("구글 토큰 교환에 실패했습니다");
            }

            return (String) response.get("access_token");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("구글 토큰 교환 중 오류 발생", e);
            throw BusinessException.badRequest("소셜 로그인 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * Access Token으로 사용자 정보 조회
     */
    public OAuthUserInfo getUserInfo(String accessToken) {
        OAuthProviderConfig.ProviderProperties props = oAuthProviderConfig.getGoogle();

        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(props.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(TIMEOUT);

            if (response == null) {
                throw BusinessException.badRequest("구글 사용자 정보 조회에 실패했습니다");
            }

            String oauthId = (String) response.get("id");
            String email = (String) response.get("email");
            String name = (String) response.get("name");
            String picture = (String) response.get("picture");

            return new OAuthUserInfo(oauthId, email, name, picture);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 중 오류 발생", e);
            throw BusinessException.badRequest("소셜 로그인 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
