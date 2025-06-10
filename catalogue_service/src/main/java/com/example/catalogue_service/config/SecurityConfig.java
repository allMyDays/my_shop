package com.example.catalogue_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .authorizeHttpRequests(authorizeHttpRequests->
                                authorizeHttpRequests
                                        .requestMatchers(HttpMethod.POST,"/catalogue-api/products")
                                        .hasAuthority("SCOPE_edit_catalogue")
                                        .requestMatchers(HttpMethod.POST,"catalogue-api/products/{productId:\\d+}")
                                        .hasAuthority("SCOPE_edit_catalogue")
                                        .requestMatchers(HttpMethod.DELETE,"catalogue-api/products/{productId:\\d+}")
                                        .hasAuthority("SCOPE_edit_catalogue")
                                        .requestMatchers(HttpMethod.GET)
                                        .hasAuthority("SCOPE_view_catalogue")
                                        .anyRequest()
                                        .denyAll()
                )
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(sess->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer->oauth2ResourceServer.jwt(Customizer.withDefaults()))
                .build();
    }




}
