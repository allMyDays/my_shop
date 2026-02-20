package com.example.catalogue_service.repository;

import com.example.catalogue_service.entity.Category;
import com.example.catalogue_service.entity.Product;
import com.example.common.enumeration.category.CategoryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    Optional<Category> findByCode(CategoryCode code);

    boolean existsByCode(CategoryCode code);








}
