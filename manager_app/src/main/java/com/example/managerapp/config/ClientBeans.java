package com.example.managerapp.config;

import com.example.managerapp.rest.ProductRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.parameters.P;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {

    @Value("${my_shop.services.catalogue.uri:http://localhost:8082}")
    private String baseUrl;




    @Bean
    public ProductRestClient productRestClient() {
       return new ProductRestClient(RestClient.builder()
               .baseUrl(baseUrl)
               .build()
       );
    }




}
