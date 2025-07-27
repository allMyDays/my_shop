package com.example.managerapp.client.rest;

import com.example.managerapp.dto.category.CategoryResponseDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class CategoryRestClient {


    private final RestClient restClient;

    private static final ParameterizedTypeReference<List<CategoryResponseDTO>> CATEGORY_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};


    public CategoryRestClient(@Qualifier("noAuth") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<CategoryResponseDTO> getAllCategories() {
        return restClient
                .get()
                .uri("/catalogue-api/categories")
                .retrieve()
                .body(CATEGORY_TYPE_REFERENCE);
    }


}
