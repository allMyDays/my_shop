package com.example.managerapp.rest;

import com.example.managerapp.entity.CategoryRecord;
import com.example.managerapp.entity.ProductRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

@RequiredArgsConstructor
public class CategoryRestClient {

    private final RestClient restClient;

    private static final ParameterizedTypeReference<List<CategoryRecord>> CATEGORY_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    public List<CategoryRecord> getAllCategories() {
        return restClient
                .get()
                .uri("/catalogue-api/categories")
                .retrieve()
                .body(CATEGORY_TYPE_REFERENCE);
    }


}
