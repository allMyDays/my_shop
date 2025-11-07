package com.example.catalogue_service.repository;


import com.example.catalogue_service.entity.Product;
import com.example.common.dto.product.ProductIdAndPriceDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    Stream<Product> findAllByIdIn(List<Long> ids);


    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.categories c " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Stream<Product> findByTitleAndOptionalCategory(String title, Long categoryId, Pageable pageable);

    @Query("SELECT p.id FROM Product p WHERE p.id IN :ids")
    List<Long> findProductIdsByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT new com.example.common.dto.product.ProductIdAndPriceDto(p.id, p.price) FROM Product p WHERE p.id IN :ids")
    List<ProductIdAndPriceDto> findIdAndPriceByIds(@Param("ids") List<Long> ids);



}

