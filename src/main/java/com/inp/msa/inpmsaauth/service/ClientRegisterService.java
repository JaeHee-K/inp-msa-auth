package com.inp.msa.inpmsaauth.service;

import com.inp.msa.inpmsaauth.dto.ClientRegisterRequestDto;
import com.inp.msa.inpmsaauth.security.CustomRegisteredClientRepository;
import com.inp.msa.inpmsaauth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientRegisterService {

    /**
     * Client의 등록 요청을 처리
     */

    private final CustomRegisteredClientRepository customRegisteredClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerClient(ClientRegisterRequestDto request) {
        String clientId = SecurityUtil.generateUUID();
        String rawClientSecret = SecurityUtil.generateSecret();
        String encodedClientSecret = passwordEncoder.encode(rawClientSecret);

        RegisteredClient.Builder registeredClientBuilder = RegisteredClient.withId(clientId)
                .clientId(clientId)
                .clientName(request.getClientName())
                .clientSecret(encodedClientSecret)
                .clientAuthenticationMethods(clientAuthMethods ->
                        clientAuthMethods.addAll(request.getClientAuthenticationMethods().stream()
                                                         .map(ClientAuthenticationMethod::new).collect(Collectors.toSet())))
                .authorizationGrantTypes(grantTypes ->
                        grantTypes.addAll(request.getAuthorizationGrantTypes().stream()
                                                  .map(AuthorizationGrantType::new).collect(Collectors.toSet())))
                .scopes(scopes -> scopes.addAll(request.getScopes()))
                .clientSettings(ClientSettings.builder().settings(settings -> settings.putAll(request.getClientSettings())).build())
                .tokenSettings(TokenSettings.builder().settings(settings -> settings.putAll(request.getTokenSettings())).build());

        // To Do Delete
        System.out.println("#############################");
        System.out.println("rawClientSecret : " + rawClientSecret);
        System.out.println("#############################");

        if (!isClientCredentialsOnly(request)) {
            registeredClientBuilder.redirectUris(redirectUris -> redirectUris.addAll(request.getRedirectUris()));
        }

        RegisteredClient registeredClient = registeredClientBuilder.build();

        // client_credential 클라이언트 분리
        if (!isClientCredentialsOnly(request)) {
            customRegisteredClientRepository.save(registeredClient);
        } else {
            customRegisteredClientRepository.saveClientCredentialsClient(registeredClient);
        }
    }

    private boolean isClientCredentialsOnly(ClientRegisterRequestDto request) {
        return request.getAuthorizationGrantTypes().contains("client_credentials")
                && request.getAuthorizationGrantTypes().size() == 1;
    }
}
