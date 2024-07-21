package com.inp.msa.inpmsaauth.security;

import com.inp.msa.inpmsaauth.domain.OauthAccessToken;
import com.inp.msa.inpmsaauth.domain.OauthTokenHistory;
import com.inp.msa.inpmsaauth.repository.OauthAccessTokenRepository;
import com.inp.msa.inpmsaauth.repository.OauthTokenHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

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
    private final CustomRegisteredClientRepository customRegisteredClientRepository; ;

    @Override
    @Transactional
    public void save(OAuth2Authorization authorization) {
        // Check if the authorization is for an access token or refresh token
        if (authorization.getAccessToken() != null) {
            saveAccessToken(authorization);
        }
        saveTokenHistory(authorization, "SAVE");
    }

    @Override
    @Transactional
    public void remove(OAuth2Authorization authorization) {
        deactivateAccessToken(authorization);
        saveTokenHistory(authorization, "REMOVE");
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Optional<OauthAccessToken> optionalOauthAccessToken = oauthAccessTokenRepository.findById(id);
        return optionalOauthAccessToken.map(this::toOAuth2Authorization).orElse(null);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Optional<OauthAccessToken> optionalOauthAccessToken = tokenType == null || OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)
                ? oauthAccessTokenRepository.findByAccessTokenValue(token)
                : oauthAccessTokenRepository.findByRefreshTokenValue(token);
        return optionalOauthAccessToken.map(this::toOAuth2Authorization).orElse(null);
    }

    private OAuth2Authorization toOAuth2Authorization(OauthAccessToken oauthAccessToken) {
        RegisteredClient registeredClient = customRegisteredClientRepository.findById(oauthAccessToken.getClientId());

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(oauthAccessToken.getTokenId())
                .principalName(oauthAccessToken.getPrincipalName())
                .authorizationGrantType(new AuthorizationGrantType(oauthAccessToken.getGrantType()))
                .token(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                                             oauthAccessToken.getAccessTokenValue(),
                                             oauthAccessToken.getAccessTokenIssuedAt() != null ? oauthAccessToken.getAccessTokenIssuedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                                             oauthAccessToken.getAccessTokenExpiresAt() != null ? oauthAccessToken.getAccessTokenExpiresAt().atZone(ZoneId.systemDefault()).toInstant() : null),
                       metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, oauthAccessToken.getScope().split(",")));

        if (oauthAccessToken.getRefreshTokenValue() != null) {
            authorizationBuilder.token(new OAuth2RefreshToken(
                    oauthAccessToken.getRefreshTokenValue(),
                    oauthAccessToken.getRefreshTokenIssuedAt() != null ? oauthAccessToken.getRefreshTokenIssuedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
                    oauthAccessToken.getRefreshTokenExpiresAt() != null ? oauthAccessToken.getRefreshTokenExpiresAt().atZone(ZoneId.systemDefault()).toInstant() : null
            ));
        }

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

    private void deactivateAccessToken(OAuth2Authorization authorization) {
        OauthAccessToken accessToken = oauthAccessTokenRepository.findById(authorization.getId())
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));
        accessToken.setStatus("INACTIVE");
        oauthAccessTokenRepository.save(accessToken);
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
