package com.example.managerapp.controller.rest;

import com.example.managerapp.entity.CategoryRecord;
import com.example.managerapp.rest.CategoryRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryRestClient categoryRestClient;


    @GetMapping
    public List<CategoryRecord> getAllCategories() {
        return categoryRestClient.getAllCategories();
    }











}
