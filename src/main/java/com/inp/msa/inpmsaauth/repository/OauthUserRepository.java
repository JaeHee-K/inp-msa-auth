package com.inp.msa.inpmsaauth.repository;

import com.inp.msa.inpmsaauth.domain.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OauthUserRepository extends JpaRepository<OauthUser, String> {
    OauthUser findByUserId(String userId);
}
