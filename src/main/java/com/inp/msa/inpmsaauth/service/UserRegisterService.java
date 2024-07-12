package com.inp.msa.inpmsaauth.service;

import com.inp.msa.inpmsaauth.domain.OauthRoles;
import com.inp.msa.inpmsaauth.domain.OauthUser;
import com.inp.msa.inpmsaauth.domain.OauthUserRoles;
import com.inp.msa.inpmsaauth.dto.UserRegisterRequestDto;
import com.inp.msa.inpmsaauth.repository.OauthRolesRepository;
import com.inp.msa.inpmsaauth.repository.OauthUserRepository;
import com.inp.msa.inpmsaauth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRegisterService {

    private final OauthUserRepository oauthUserRepository;
    private final OauthRolesRepository oauthRolesRepository;
    private final PasswordEncoder passwordEncoder;

    public void save(UserRegisterRequestDto request) {
        // User
        OauthUser oauthUser = new OauthUser();

        String salt = SecurityUtil.generateSalt();
        String saltedPassword = request.getPassword() + salt;

        oauthUser.setUserId(SecurityUtil.generateUUID());
        oauthUser.setUserAccount(request.getAccount());
        oauthUser.setPassword(passwordEncoder.encode(saltedPassword));
        oauthUser.setUserName(request.getUsername());
        oauthUser.setSalt(salt);
        oauthUser.setCreateDate(LocalDateTime.now());

        // Role
        OauthRoles oauthRoles = oauthRolesRepository.findByRoleName("ROLE_USER");

        OauthUserRoles oauthUserRoles = new OauthUserRoles();
        oauthUserRoles.setOauthUser(oauthUser);
        oauthUserRoles.setOauthRoles(oauthRoles);
        oauthUserRoles.setCreateDate(LocalDateTime.now());

        Set<OauthUserRoles> oauthUserRolesSet = new HashSet<>();
        oauthUserRolesSet.add(oauthUserRoles);
        oauthUser.setOauthUser(oauthUserRolesSet);

        oauthUserRepository.save(oauthUser);
    }
}
