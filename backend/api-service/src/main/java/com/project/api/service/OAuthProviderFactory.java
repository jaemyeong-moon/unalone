package com.project.api.service;

import com.project.api.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OAuth 제공자 클라이언트 팩토리
 * 제공자명으로 적절한 OAuthProviderClient 구현체를 반환한다.
 */
@Component
public class OAuthProviderFactory {

    private final Map<String, OAuthProviderClient> clients;

    public OAuthProviderFactory(List<OAuthProviderClient> providerClients) {
        this.clients = providerClients.stream()
                .collect(Collectors.toMap(OAuthProviderClient::getProviderName, Function.identity()));
    }

    /**
     * 제공자명으로 OAuth 클라이언트를 반환한다.
     *
     * @param provider OAuth 제공자명 (kakao, google)
     * @return OAuthProviderClient 구현체
     * @throws BusinessException 지원하지 않는 제공자인 경우
     */
    public OAuthProviderClient getClient(String provider) {
        OAuthProviderClient client = clients.get(provider.toLowerCase());
        if (client == null) {
            throw BusinessException.badRequest("지원하지 않는 OAuth 제공자입니다: " + provider);
        }
        return client;
    }

    /**
     * 지원하는 제공자 목록을 반환한다.
     */
    public List<String> getSupportedProviders() {
        return List.copyOf(clients.keySet());
    }
}
