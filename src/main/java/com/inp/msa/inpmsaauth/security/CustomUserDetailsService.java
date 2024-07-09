package com.inp.msa.inpmsaauth.security;

import com.inp.msa.inpmsaauth.domain.OauthUser;
import com.inp.msa.inpmsaauth.domain.OauthUserRoles;
import com.inp.msa.inpmsaauth.repository.OauthUserRepository;
import com.inp.msa.inpmsaauth.repository.OauthUserRolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final OauthUserRepository oauthUserRepository;
    private final OauthUserRolesRepository oauthUserRolesRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userAccount) throws UsernameNotFoundException {
        OauthUser oauthUser = oauthUserRepository.findByUserAccount(userAccount);
        if (oauthUser == null) {
            throw new UsernameNotFoundException(userAccount + " User not found");
        }

        List<OauthUserRoles> oauthUserRoles = oauthUserRolesRepository.findByOauthUser(oauthUser);
        return new CustomUserDetails(oauthUser, oauthUserRoles);
    }
}
