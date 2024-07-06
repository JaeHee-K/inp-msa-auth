package com.inp.msa.inpmsaauth.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

public class SecurityCommonConfig {

    /**
     * SecurityCommonConfig는 공통 보안 설정을 관리
     */

    public static void configureCommonSettings(HttpSecurity http) throws Exception {
        http
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                )
                .csrf(AbstractHttpConfigurer::disable);
    }
}
