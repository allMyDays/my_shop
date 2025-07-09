package com.example.catalogue_service.repository;


import com.example.catalogue_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findByTitle(String title);

    List<Product> findAllByTitleLikeIgnoreCase(String title);

    List<Product> findAllByIdIn(List<Long> ids);


    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.categories c " +
            "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    List<Product> findByCategoryAndTitle(Long categoryId, String title);

}