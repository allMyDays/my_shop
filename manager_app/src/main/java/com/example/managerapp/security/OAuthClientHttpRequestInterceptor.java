package com.example.managerapp.security;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.*;

import java.io.IOException;
@RequiredArgsConstructor
public class OAuthClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {   // кастомный перехватчик запросов

    private final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;    // менеджер по токенам, умеет получать токен у keycloak и обновлять при необходимости

    private final String registrationID;

    @Setter
    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy(); // хранение данных о текущем пользователе


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
           OAuth2AuthorizedClient authorizedClient = this.oAuth2AuthorizedClientManager.authorize(
                   OAuth2AuthorizeRequest.withClientRegistrationId(registrationID)   // класс-заявка на получение токена
                           .principal(securityContextHolderStrategy.getContext().getAuthentication())
                           .build());
           request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());


        }
        return execution.execute(request, body);

    }
}
