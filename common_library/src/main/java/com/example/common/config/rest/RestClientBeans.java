package com.example.common.config.rest;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnClass(value = {
        RestClient.class,
        DefaultOAuth2AuthorizedClientManager.class,
        OAuth2AuthorizedClientManager.class,
        ClientRegistrationRepository.class,
        OAuth2AuthorizedClientManager.class
})
@ConditionalOnBean({
        ClientRegistrationRepository.class,
        OAuth2AuthorizedClientManager.class
})
public class RestClientBeans {

    @Bean
    @Scope("prototype")
    public RestClient.Builder authRestClientBuilder(
            ClientRegistrationRepository clientRepo,
            OAuth2AuthorizedClientRepository authClientRepo,
        @Value("${library.registration-id}") String regId) {

        OAuth2AuthorizedClientManager clientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRepo, authClientRepo);

        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                        var auth = SecurityContextHolder.getContext().getAuthentication();
                        var client = clientManager.authorize(
                                OAuth2AuthorizeRequest.withClientRegistrationId(regId)
                                        .principal(auth)
                                        .build());
                        if(client==null){
                            throw new IllegalStateException("Client not found");
                        }
                        request.getHeaders().setBearerAuth(client.getAccessToken().getTokenValue());
                    }
                    return execution.execute(request, body);
                });
    }


    @Bean
    @Lazy
    public RestClient noAuthCatalogueRestClient(EurekaClient eurekaClient) {
        InstanceInfo instanceInfo = eurekaClient.getApplication("catalogue-service")
                .getInstances()
                .get(0);

        String address = instanceInfo.getHostName()+":"+instanceInfo.getPort();

        return RestClient.builder()
                .baseUrl(address)
                .build();

    }

    @Bean
    @Lazy
    public RestClient authCatalogueRestClient(EurekaClient eurekaClient, ObjectProvider<RestClient.Builder> objectProvider) {
        InstanceInfo instanceInfo = eurekaClient.getApplication("catalogue-service")
                .getInstances()
                .get(0);

        String address = instanceInfo.getHostName()+":"+instanceInfo.getPort();

        return objectProvider.getObject()
                .baseUrl(address)
                .build();

    }

    @Bean
    @Lazy
    public RestClient authSupportRestClient(EurekaClient eurekaClient, ObjectProvider<RestClient.Builder> objectProvider) {
        InstanceInfo instanceInfo = eurekaClient.getApplication("support-service")
                .getInstances()
                .get(0);

        String address = instanceInfo.getHostName()+":"+instanceInfo.getPort();

        return objectProvider.getObject()
                .baseUrl(address)
                .build();

    }


















}
