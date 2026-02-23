package com.example.frontend.config;

import com.example.common.security.JwtRolesPostProcessingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity()
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRolesPostProcessingFilter jwtRolesPostProcessingFilter) throws Exception {

        return http
                .authorizeHttpRequests(authorizeHttpRequests->
                        authorizeHttpRequests
                                .anyRequest()
                                .permitAll()
                )
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(sess->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer->oauth2ResourceServer.jwt(Customizer.withDefaults()))
                .addFilterAfter(jwtRolesPostProcessingFilter, BearerTokenAuthenticationFilter.class /*валидирует токен*/)
                .build();
    }




}