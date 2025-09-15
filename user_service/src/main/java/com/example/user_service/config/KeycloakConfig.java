package com.example.user_service.config;

import com.example.user_service.service.UserKeycloakService;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class KeycloakConfig {


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KeycloakBuilder keycloakBuilder(@Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuerUri,
                             @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId,
                             @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}") String clientSecret){

        return KeycloakBuilder.builder()
                .serverUrl(UserKeycloakService.extractServerUri(issuerUri))
                .realm(UserKeycloakService.extractRealm(issuerUri))
                .clientId(clientId)
                .clientSecret(clientSecret);



   }



}
