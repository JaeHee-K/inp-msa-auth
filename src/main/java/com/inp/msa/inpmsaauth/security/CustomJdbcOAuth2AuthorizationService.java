package com.inp.msa.inpmsaauth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

@RequiredArgsConstructor
public class CustomJdbcOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(OAuth2Authorization authorization) {

    }

    @Override
    public void remove(OAuth2Authorization authorization) {

    }

    @Override
    public OAuth2Authorization findById(String id) {
        return null;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return null;
    }

    private void saveAccessToken(OAuth2Authorization authorization) {

    }

    private void deactivateAccessToken(OAuth2Authorization authorization) {

    }

    private void saveTokenHistory(OAuth2Authorization authorization, String action) {
        
    }
}
