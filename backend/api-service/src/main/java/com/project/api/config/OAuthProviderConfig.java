package com.project.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "oauth")
@Getter
@Setter
public class OAuthProviderConfig {

    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("kakao", "google");

    private ProviderProperties kakao;
    private ProviderProperties google;
    private String frontendRedirectUri;

    public ProviderProperties getProvider(String provider) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> kakao;
            case "google" -> google;
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
        };
    }

    public boolean isSupported(String provider) {
        return SUPPORTED_PROVIDERS.contains(provider.toLowerCase());
    }

    @Getter
    @Setter
    public static class ProviderProperties {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
        private String authorizationUri;
        private String scope;
    }
}
