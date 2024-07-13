package com.inp.msa.inpmsaauth.repository;

import com.inp.msa.inpmsaauth.domain.OauthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthClientRepository extends JpaRepository<OauthClient, String> {

    Optional<OauthClient> findByClientId(String clientId);
}
