package com.inp.msa.inpmsaauth.repository;

import com.inp.msa.inpmsaauth.domain.OauthRoles;
import com.inp.msa.inpmsaauth.domain.OauthUser;
import com.inp.msa.inpmsaauth.domain.OauthUserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OauthUserRolesRepository extends JpaRepository<OauthUserRoles, Long> {
    OauthUserRoles findByUserRoleId(Long userRoleId);
    List<OauthUserRoles> findByOauthUser(OauthUser oauthUser);
    List<OauthUserRoles> findByOauthRoles(OauthRoles oauthRoles);
}
