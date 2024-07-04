package com.inp.msa.inpmsaauth.repository;

import com.inp.msa.inpmsaauth.domain.OauthRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OauthRolesRepository extends JpaRepository<OauthRoles, Long> {
    OauthRoles findByRoleId(Long roleId);
}
