package com.example.managerapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(
                        a->a
                                .anyRequest()
                                .hasRole("CUSTOMER"))
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    public OAuth2UserService<OidcUserRequest // обьект-запрос, содержащий всё что спринг получил от keycloak
            , OidcUser> // описывает пользователя вошелшего через OIDC (keycloak, google, etc)
             oAuth2UserService() {
       return new OAuth2UserService<OidcUserRequest, OidcUser>() {
           @Override
           public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
               OidcUser oidcUser = new OidcUserService().loadUser(userRequest);
               List<GrantedAuthority> authorities =    //  получаем все роли юзера из keycloak
                       Stream.concat(oidcUser.getAuthorities().stream(), Optional.ofNullable(oidcUser.getClaimAsStringList("groups"))
                               .orElseGet(List::of)
                               .stream()
                               .filter(role->role.startsWith("ROLE_"))
                               .map(SimpleGrantedAuthority::new)
                               .map(GrantedAuthority.class::cast))
                               .toList();

               return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());

           }
       };

    }







}
