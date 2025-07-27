package com.example.managerapp.controller.rest;

import com.example.managerapp.client.grpc.CategoryGrpcClient;
import com.example.managerapp.client.rest.CategoryRestClient;
import com.example.managerapp.dto.category.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryGrpcClient categoryGrpcClient;


    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryGrpcClient.getAllCategories();
    }











}
