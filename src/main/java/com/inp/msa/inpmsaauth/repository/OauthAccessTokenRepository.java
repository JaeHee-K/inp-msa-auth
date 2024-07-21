package com.inp.msa.inpmsaauth.repository;

import com.inp.msa.inpmsaauth.domain.OauthAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthAccessTokenRepository extends JpaRepository<OauthAccessToken, String> {

    Optional<OauthAccessToken> findByTokenId(String token);

    Optional<OauthAccessToken> findByAccessTokenValue(String accessTokenValue);

    Optional<OauthAccessToken> findByRefreshTokenValue(String refreshTokenValue);
}
