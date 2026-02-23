package com.example.common.client.rest;

import com.example.common.dto.category.rest.CategoryResponseDTO;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@ConditionalOnClass(value={RestClient.Builder.class, EurekaClient.class})
@ConditionalOnBean(RestClient.Builder.class)
public class CategoryRestClient {

    private RestClient restClient;

    private static final ParameterizedTypeReference<List<CategoryResponseDTO>> CATEGORY_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};


    @Autowired
    @Lazy
    public void setRestClient(@Qualifier("noAuthCatalogueRestClient") RestClient restClient) {
        this.restClient = restClient;
    }


    public List<CategoryResponseDTO> getAllCategories() {
        return restClient
                .get()
                .uri("/api/categories")
                .retrieve()
                .body(CATEGORY_TYPE_REFERENCE);
    }


}
