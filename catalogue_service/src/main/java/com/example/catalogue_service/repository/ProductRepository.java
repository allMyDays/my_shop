package com.example.catalogue_service.repository;


import com.example.catalogue_service.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    Stream<Product> findAllByIdIn(List<Long> ids, Pageable pageable);


    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.categories c " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Stream<Product> findByTitleAndOptionalCategory(String title, Long categoryId, Pageable pageable);

}