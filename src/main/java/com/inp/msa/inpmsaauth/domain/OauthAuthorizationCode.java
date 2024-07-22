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
@Table(name = "oauth_authorization_code")
public class OauthAuthorizationCode {

    @Id
    @Column(name = "code_id")
    private String codeId;

    @Column(name = "code_value")
    private String codeValue;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "principal_name")
    private String principalName;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
