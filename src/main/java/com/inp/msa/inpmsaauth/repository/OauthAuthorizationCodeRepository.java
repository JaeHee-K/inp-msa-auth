package com.inp.msa.inpmsaauth.repository;

import com.inp.msa.inpmsaauth.domain.OauthAuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthAuthorizationCodeRepository extends JpaRepository<OauthAuthorizationCode, String> {

    Optional<OauthAuthorizationCode> findByCodeValue(String codeValue);
}
