package com.inp.msa.inpmsaauth.security;

import com.inp.msa.inpmsaauth.domain.OauthAccessToken;
import com.inp.msa.inpmsaauth.domain.OauthAuthorizationCode;
import com.inp.msa.inpmsaauth.domain.OauthTokenHistory;
import com.inp.msa.inpmsaauth.repository.OauthAccessTokenRepository;
import com.inp.msa.inpmsaauth.repository.OauthAuthorizationCodeRepository;
import com.inp.msa.inpmsaauth.repository.OauthTokenHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Primary
public class CustomOAuth2AuthorizationService implements OAuth2AuthorizationService {

    /**
     * OAuth2AuthorizationService는 Spring Security에서 OAuth 2.0 Authorization 관련 데이터를 관리하는 인터페이스임
     * 액세스 토큰, 리프레시 토큰, 권한 코드 등을 저장, 검색, 삭제하는 기능을 제공함
     * 해당 클래스를 통해 OAuth 2.0 클라이언트와 관련된 데이터의 상태를 유지하고 관리할 수 있음
     */

    private final OauthAccessTokenRepository oauthAccessTokenRepository;
    private final OauthTokenHistoryRepository oauthTokenHistoryRepository;
    private final CustomRegisteredClientRepository customRegisteredClientRepository;
    private final OauthAuthorizationCodeRepository oauthAuthorizationCodeRepository;

    @Override
    @Transactional
    public void save(OAuth2Authorization authorization) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (request != null && "/oauth2/revoke".equals(request.getRequestURI())) {
            remove(authorization);
            return;
        }

        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorization.getAuthorizationGrantType())) {
            if (authorization.getAccessToken() == null) {
                saveAuthorizationCode(authorization);
            } else {
                saveAccessToken(authorization);
            }
        } else {
            saveAccessToken(authorization);
        }

        saveTokenHistory(authorization, "SAVE");
    }

    @Override
    @Transactional
    public void remove(OAuth2Authorization authorization) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorization.getAuthorizationGrantType())) {
            removeAuthorizationCode(authorization);
        } else {
            deactivateAccessToken(authorization);
        }

        saveTokenHistory(authorization, "REMOVE");
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Optional<OauthAccessToken> optionalOauthAccessToken = oauthAccessTokenRepository.findById(id);
        if (optionalOauthAccessToken.isPresent()) {
            return optionalOauthAccessToken.map(this::toOAuth2Authorization).orElse(null);
        }

        Optional<OauthAuthorizationCode> optionalOauthAuthorizationCode = oauthAuthorizationCodeRepository.findById(id);
        return optionalOauthAuthorizationCode.map(this::toOAuth2Authorization).orElse(null);
    }

    /*
    token introspect, revoke 등 조회가 필요할 때 사용
     */
    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        if (tokenType == null || OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            Optional<OauthAccessToken> optionalOauthAccessToken = oauthAccessTokenRepository.findByAccessTokenValue(token);
            return optionalOauthAccessToken
                    .filter(accessToken -> !"INACTIVE".equals(accessToken.getStatus()))
                    .map(this::toOAuth2Authorization)
                    .orElse(null);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            Optional<OauthAccessToken> optionalOauthAccessToken = oauthAccessTokenRepository.findByRefreshTokenValue(token);
            return optionalOauthAccessToken
                    .filter(refreshToken -> !"INACTIVE".equals(refreshToken.getStatus()))
                    .map(this::toOAuth2Authorization)
                    .orElse(null);
        } else if ("code".equals(tokenType.getValue())) {
            Optional<OauthAuthorizationCode> optionalOauthAuthorizationCode = oauthAuthorizationCodeRepository.findByCodeValue(token);
            return optionalOauthAuthorizationCode.map(this::toOAuth2Authorization).orElse(null);
        }
        return null;
    }

    /*
    client credential 용도의 toOAuth2Authorization
     */
    private OAuth2Authorization toOAuth2Authorization(OauthAccessToken oauthAccessToken) {
        if ("INACTIVE".equals(oauthAccessToken.getStatus())) {
            return null;
        }

        RegisteredClient registeredClient = customRegisteredClientRepository.findById(oauthAccessToken.getClientId());

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(oauthAccessToken.getTokenId())
                .principalName(oauthAccessToken.getPrincipalName())
                .authorizationGrantType(new AuthorizationGrantType(oauthAccessToken.getGrantType()))
                .token(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                                             oauthAccessToken.getAccessTokenValue(),
                                             oauthAccessToken.getAccessTokenIssuedAt() != null ? oauthAccessToken.getAccessTokenIssuedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                                             oauthAccessToken.getAccessTokenExpiresAt() != null ? oauthAccessToken.getAccessTokenExpiresAt().atZone(ZoneId.systemDefault()).toInstant() : null),
                       metadata -> {
                            if (oauthAccessToken.getScope() != null) {
                                Map<String, Object> claims = new HashMap<>();
                                claims.put("scope", String.join(" ", oauthAccessToken.getScope().split(",")));
                                metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claims);
                            }
                       });

        if (oauthAccessToken.getRefreshTokenValue() != null) {
            authorizationBuilder.token(new OAuth2RefreshToken(
                    oauthAccessToken.getRefreshTokenValue(),
                    oauthAccessToken.getRefreshTokenIssuedAt() != null ? oauthAccessToken.getRefreshTokenIssuedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                    oauthAccessToken.getRefreshTokenExpiresAt() != null ? oauthAccessToken.getRefreshTokenExpiresAt().atZone(ZoneId.systemDefault()).toInstant() : null
            ));
        }

        return authorizationBuilder.build();
    }

    /*
    authorization code 용도의 toOAuth2Authorization
     */
    private OAuth2Authorization toOAuth2Authorization(OauthAuthorizationCode oauthAuthorizationCode) {
        RegisteredClient registeredClient = customRegisteredClientRepository.findById(oauthAuthorizationCode.getClientId());

        // authorizedScopes Setting
        Set<String> scopes = Set.of();
        if (oauthAuthorizationCode.getScopes() != null && !oauthAuthorizationCode.getScopes().isEmpty()) {
            scopes = Set.of(oauthAuthorizationCode.getScopes().split(","));
        }

        // authorizationRequest Setting
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .clientId(oauthAuthorizationCode.getClientId())
                .authorizationUri(oauthAuthorizationCode.getAuthorizationUri())
                .redirectUri(oauthAuthorizationCode.getRedirectUri())
                .scopes(scopes)
                .state(oauthAuthorizationCode.getState())
                .additionalParameters(Map.of())
                .build();

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                oauthAuthorizationCode.getPrincipalName(), null, Collections.emptyList());

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(oauthAuthorizationCode.getCodeId())
                .principalName(oauthAuthorizationCode.getPrincipalName())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .token(new OAuth2AuthorizationCode(
                        oauthAuthorizationCode.getCodeValue(),
                        oauthAuthorizationCode.getIssuedAt() != null ? oauthAuthorizationCode.getIssuedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                        oauthAuthorizationCode.getExpiresAt() != null ? oauthAuthorizationCode.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant() : null
                ))
                .attribute(OAuth2AuthorizationRequest.class.getName(), authorizationRequest)
                .attribute(Principal.class.getName(), authentication);

        authorizationBuilder.attribute(OAuth2AuthorizationRequest.class.getName(),authorizationRequest);

        return authorizationBuilder.build();
    }

    private void saveAccessToken(OAuth2Authorization authorization) {
        OauthAccessToken accessToken = new OauthAccessToken();
        accessToken.setTokenId(authorization.getId());
        accessToken.setClientId(authorization.getRegisteredClientId());
        accessToken.setPrincipalName(authorization.getPrincipalName());
        accessToken.setGrantType(authorization.getAuthorizationGrantType().getValue());

        // Access Token
        OAuth2AccessToken token = authorization.getAccessToken().getToken();
        accessToken.setAccessTokenValue(token.getTokenValue());
        accessToken.setScope(String.join(",", token.getScopes()));

        if (token.getIssuedAt() != null) {
            accessToken.setAccessTokenIssuedAt(LocalDateTime.ofInstant(token.getIssuedAt(), ZoneId.systemDefault()));
        }
        if (token.getExpiresAt() != null) {
            accessToken.setAccessTokenExpiresAt(LocalDateTime.ofInstant(token.getExpiresAt(), ZoneId.systemDefault()));
        }

        // Refresh Token
        OAuth2RefreshToken refreshToken = authorization.getRefreshToken() != null ? authorization.getRefreshToken().getToken() : null;
        if (refreshToken != null) {
            accessToken.setRefreshTokenValue(refreshToken.getTokenValue());
            if (refreshToken.getIssuedAt() != null) {
                accessToken.setRefreshTokenIssuedAt(LocalDateTime.ofInstant(refreshToken.getIssuedAt(), ZoneId.systemDefault()));
            }
            if (refreshToken.getExpiresAt() != null) {
                accessToken.setRefreshTokenExpiresAt(LocalDateTime.ofInstant(refreshToken.getExpiresAt(), ZoneId.systemDefault()));
            }
        }

        accessToken.setCreateDate(LocalDateTime.now());
        accessToken.setStatus("ACTIVE");

        oauthAccessTokenRepository.save(accessToken);
    }

    private void saveAuthorizationCode(OAuth2Authorization authorization) {
        RegisteredClient registeredClient = customRegisteredClientRepository.findById(authorization.getRegisteredClientId());

        OAuth2Authorization.Token<?> authorizationCodeToken = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationCodeToken == null) {
            throw new IllegalArgumentException("Authorization code not found in authorization");
        }

        OauthAuthorizationCode code = new OauthAuthorizationCode();
        code.setCodeId(authorization.getId());
        code.setCodeValue(authorizationCodeToken.getToken().getTokenValue());
        code.setClientId(authorization.getRegisteredClientId());
        code.setPrincipalName(authorization.getPrincipalName());
        code.setScopes(String.join(",", authorization.getAuthorizedScopes()));

        OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
        if (authorizationRequest != null) {
            code.setAuthorizationUri(authorizationRequest.getAuthorizationUri());
            code.setRedirectUri(authorizationRequest.getRedirectUri());
            code.setState(authorizationRequest.getState());
        }

        code.setIssuedAt(LocalDateTime.ofInstant(authorizationCodeToken.getToken().getIssuedAt(), ZoneId.systemDefault()));

        Duration authorizationCodeTimeToLive = registeredClient.getTokenSettings().getAuthorizationCodeTimeToLive();
        if (authorizationCodeTimeToLive != null) {
            code.setExpiresAt(LocalDateTime.ofInstant(authorizationCodeToken.getToken().getIssuedAt().plus(authorizationCodeTimeToLive), ZoneId.systemDefault()));
        }

        oauthAuthorizationCodeRepository.save(code);
    }

    private void deactivateAccessToken(OAuth2Authorization authorization) {
        OauthAccessToken accessToken = oauthAccessTokenRepository.findById(authorization.getId())
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));
        accessToken.setStatus("INACTIVE");
        oauthAccessTokenRepository.save(accessToken);
    }

    private void removeAuthorizationCode(OAuth2Authorization authorization) {
        OauthAuthorizationCode code = oauthAuthorizationCodeRepository.findById(authorization.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authorization code not found"));
        oauthAuthorizationCodeRepository.delete(code);
    }

    private void saveTokenHistory(OAuth2Authorization authorization, String action) {
        OauthTokenHistory tokenHistory = new OauthTokenHistory();
        tokenHistory.setTokenId(authorization.getId());
        tokenHistory.setClientId(authorization.getRegisteredClientId());
        tokenHistory.setPrincipalName(authorization.getPrincipalName());
        tokenHistory.setAction(action);
        tokenHistory.setCreateDate(LocalDateTime.now());

        oauthTokenHistoryRepository.save(tokenHistory);
    }
}
