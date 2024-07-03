package com.inp.msa.inpmsaauth.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "oauth_roles")
public class OauthRoles {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name")
    private String roleName;

    @OneToMany(mappedBy = "oauthRoles", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OauthUserRoles> oauthRoles = new HashSet<>();
}
