package com.example.managerapp.config;

import com.example.managerapp.security.OAuthClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientBeans {


    @Bean
    @Qualifier("withAuth")
    public RestClient restClientWithAuthentication(
            @Value("${my_shop.services.catalogue.uri:http://localhost:8082}") String baseUrl,
            @Value("${my_shop.services.catalogue.registration-id:keycloak}") String registrationID,
            ClientRegistrationRepository clientRegistrationRepository,              // класс для хранения client_id, client_secret из конфига
            OAuth2AuthorizedClientRepository authorizedClientRepository){            // хранилище выданный токенов пользователям, чтобы каждый раз не выдывать новый

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(
                        new OAuthClientHttpRequestInterceptor(
                                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository), registrationID))
                .build();



    }
    @Bean
    @Qualifier("noAuth")
    public RestClient restClientWithNoAuthentication(
            @Value("${my_shop.services.catalogue.uri:http://localhost:8082}") String baseUrl){

            return RestClient.builder()
                .baseUrl(baseUrl)
                .build();

    }





}
