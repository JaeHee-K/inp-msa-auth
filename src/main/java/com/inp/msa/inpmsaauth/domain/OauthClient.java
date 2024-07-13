package com.inp.msa.inpmsaauth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "oauth_client")
public class OauthClient {

    @Id
    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "client_authentication_methods", columnDefinition = "json")
    private String clientAuthenticationMethods;

    @Column(name = "authorization_grant_types", columnDefinition = "json")
    private String authorizationGrantTypes;

    @Column(name = "redirect_uris", columnDefinition = "json")
    private String redirectUris;

    @Column(name = "scopes", columnDefinition = "json")
    private String scopes;

    @Column(name = "client_settings", columnDefinition = "json")
    private String clientSettings;

    @Column(name = "token_settings", columnDefinition = "json")
    private String tokenSettings;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        updateDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}
