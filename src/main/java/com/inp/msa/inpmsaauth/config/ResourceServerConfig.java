package com.inp.msa.inpmsaauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

public class ResourceServerConfig {

    /**
     * ResourceServerConfig는 리소스 서버와 관련된 설정을 담당
     * 주로 JWT 토큰을 사용하여 보호된 리소스에 대한 접근을 제어
     */

    @Bean
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        //OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http
                .authorizeHttpRequests(request -> request
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter GrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        GrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        GrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(GrantedAuthoritiesConverter);
        return converter;
    }
}
