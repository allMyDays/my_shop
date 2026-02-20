package com.example.catalogue_service.service;

import com.example.catalogue_service.entity.Category;
import com.example.catalogue_service.repository.CategoryRepository;
import com.example.common.enumeration.category.CategoryCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findCategoryByName(@NonNull String categoryName) {
        return categoryRepository.findByName(categoryName);
    }

    public Optional<Category> getCategoryByCode(@NonNull CategoryCode categoryCode) {
        return categoryRepository.findByCode(categoryCode);
    }




















}
