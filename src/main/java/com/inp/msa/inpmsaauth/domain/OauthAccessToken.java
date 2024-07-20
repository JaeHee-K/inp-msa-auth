package com.inp.msa.inpmsaauth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "oauth_access_token")
public class OauthAccessToken {

    @Id
    @Column(name = "token_id")
    private String tokenId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "principal_name")
    private String principalName;

    @Column(name = "scope")
    private String scope;

    @Column(name = "access_token_value")
    private String accessTokenValue;

    @Column(name = "access_token_issued_at")
    private LocalDateTime accessTokenIssuedAt;

    @Column(name = "access_token_expires_at")
    private LocalDateTime accessTokenExpiresAt;

    @Column(name = "refresh_token_value")
    private String refreshTokenValue;

    @Column(name = "refresh_token_issued_at")
    private LocalDateTime refreshTokenIssuedAt;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "status")
    private String status;

}
