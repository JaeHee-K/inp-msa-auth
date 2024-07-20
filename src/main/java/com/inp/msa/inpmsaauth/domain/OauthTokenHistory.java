package com.inp.msa.inpmsaauth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "oauth_token_history")
public class OauthTokenHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "token_id")
    private String tokenId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "principal_name")
    private String principalName;

    @Column(name = "action")
    private String action;

    @Column(name = "create_date")
    private LocalDateTime createDate;
}
