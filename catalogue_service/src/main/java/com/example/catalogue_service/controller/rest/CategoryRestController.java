package com.example.catalogue_service.controller.rest;

import com.example.catalogue_service.mapper.LocalCategoryMapper;
import com.example.catalogue_service.service.CategoryService;
import com.example.common.dto.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalogue/categories")
public class CategoryRestController {

    private final CategoryService categoryService;

    private final LocalCategoryMapper localCategoryMapper;

    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        return localCategoryMapper.toCategoryResponseDTOs(categoryService.findAllCategories());
    }










}
