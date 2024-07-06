package com.inp.msa.inpmsaauth.config;

import com.inp.msa.inpmsaauth.domain.OauthUser;
import com.inp.msa.inpmsaauth.domain.OauthUserRoles;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final OauthUser oauthUser;
    private final List<OauthUserRoles> oauthUserRoles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauthUserRoles.stream()
                .map(oauthUserRole -> new SimpleGrantedAuthority(oauthUserRole.getOauthRoles().getRoleName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return oauthUser.getPassword();
    }

    @Override
    public String getUsername() {
        return oauthUser.getUserAccount();
    }


    /**
     * To-Do Custom
     */
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

}
