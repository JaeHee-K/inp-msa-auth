package com.inp.msa.inpmsaauth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.inp.msa.inpmsaauth.domain.OauthClient;
import com.inp.msa.inpmsaauth.repository.OauthClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    /**
     * RegisteredClientRepository 인터페이스 구현
     * 1. RegisteredClient 객체를 OauthClient로 변환하여 저장
     * 2. DB에 저장된 엔티티를 RegisteredClient 객체로 변환하여 로드
     * 3. deserialize을 위한 일부 추가 메소드
     */

    private final OauthClientRepository oauthClientRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void save(RegisteredClient registeredClient) {
        OauthClient client = new OauthClient();
        client.setClientId(registeredClient.getClientId());
        client.setClientName(registeredClient.getClientName());
        client.setClientSecret(registeredClient.getClientSecret());
        client.setClientAuthenticationMethods(serialize(registeredClient.getClientAuthenticationMethods().stream()
                .map(ClientAuthenticationMethod::getValue).collect(Collectors.toSet())));
        client.setAuthorizationGrantTypes(serialize(registeredClient.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue).collect(Collectors.toSet())));
        client.setRedirectUris(serialize(registeredClient.getRedirectUris()));
        client.setScopes(serialize(registeredClient.getScopes()));
        client.setClientSettings(serialize(registeredClient.getClientSettings().getSettings()));
        client.setTokenSettings(serialize(registeredClient.getTokenSettings().getSettings()));

        oauthClientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return oauthClientRepository.findById(id)
                .map(this::mapToRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return oauthClientRepository.findByClientId(clientId)
                .map(this::mapToRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient mapToRegisteredClient(OauthClient client) {
        return RegisteredClient.withId(client.getClientId())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientAuthenticationMethods(clientAuthMethods ->
                                                     deserializeToSet(client.getClientAuthenticationMethods(), String.class)
                                                             .forEach(method -> clientAuthMethods.add(new ClientAuthenticationMethod(method))))
                .authorizationGrantTypes(grantTypes ->
                                                 deserializeToSet(client.getAuthorizationGrantTypes(), String.class)
                                                         .forEach(grantType -> grantTypes.add(new AuthorizationGrantType(grantType))))
                .redirectUris(redirectUris ->
                                      redirectUris.addAll(deserializeToSet(client.getRedirectUris(), String.class)))
                .scopes(scopes ->
                        scopes.addAll(deserializeToSet(client.getScopes(), String.class)))
                .clientSettings(mapToClientSettings(client.getClientSettings()))
                .tokenSettings(mapToTokenSettings(client.getTokenSettings()))
                .build();
    }

    private ClientSettings mapToClientSettings(String clientSettingsJson) {
        try {
            Map<String, Object> clientSettingsMap = deserializeToMap(clientSettingsJson, String.class, Object.class);
            ClientSettings.Builder builder = ClientSettings.builder();
            clientSettingsMap.forEach(builder::setting);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TokenSettings mapToTokenSettings(String tokenSettingsJson) {
        try {
            Map<String, Object> tokenSettingsMap = deserializeToMap(tokenSettingsJson, String.class, Object.class);
            TokenSettings.Builder builder = TokenSettings.builder();
            tokenSettingsMap.forEach((key, value) -> {
                if (value instanceof String && isDurationKey(key)) {
                    builder.setting(key, Duration.parse((String) value));
                } else if (value instanceof Map && isOAuth2TokenFormatKey(key)) {
                    LinkedHashMap<String, String> formatMap = (LinkedHashMap<String, String>) value;
                    builder.setting(key, new OAuth2TokenFormat(formatMap.get("value")));
                } else if (value instanceof String && isSignatureAlgorithmKey(key)) {
                    builder.setting(key, SignatureAlgorithm.from((String) value));
                } else {
                    builder.setting(key, value);
                }
            });
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Set<T> deserializeToSet(String json, Class<T> clazz) {
        try {
            CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(Set.class, clazz);
            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <K, V> Map<K, V> deserializeToMap(String json, Class<K> keyClass, Class<V> valueClass) {
        try {
            MapType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Token Key:Value Validation
     */
    private boolean isDurationKey(String key) {
        return key.equals("settings.token.authorization-code-time-to-live") ||
                key.equals("settings.token.access-token-time-to-live") ||
                key.equals("settings.token.refresh-token-time-to-live") ||
                key.equals("settings.token.device-code-time-to-live");
    }

    private boolean isOAuth2TokenFormatKey(String key) {
        return key.equals("settings.token.access-token-format");
    }

    private boolean isSignatureAlgorithmKey(String key) {
        return key.equals("settings.token.id-token-signature-algorithm");
    }
}
