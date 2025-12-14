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

    @Query("""
         SELECT DISTINCT p FROM Product p
         JOIN p.categories c 
         WHERE c.id = :categoryId
         """)
    Stream<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);



    @Query("""
      SELECT p FROM Product p 
      WHERE LOWER(p.title) LIKE LOWER(CONCAT('%',:title,'%'))
      """)
    Stream<Product> findByTitle(@Param("title") String title, Pageable pageable);


    @Query("""
    SELECT DISTINCT p FROM Product p
    JOIN p.categories c
    WHERE c.id = :categoryId
     AND LOWER(p.title) LIKE LOWER(CONCAT('%',:title,'%'))
    """)
    Stream<Product> findByTitleAndCategory(@Param("title") String title, @Param("categoryId") Long categoryId, Pageable pageable);









    Stream<Product> findAllByIdIn(List<Long> ids);
    
    

    @Query("SELECT p.id FROM Product p WHERE p.id IN :ids")
    List<Long> findProductIdsByIdIn(@Param("ids") List<Long> ids);
    
    
    


    @Query("SELECT new com.example.common.dto.product.ProductIdAndPriceDto(p.id, p.price) FROM Product p WHERE p.id IN :ids")
    List<ProductIdAndPriceDto> findIdAndPriceByIds(@Param("ids") List<Long> ids);



}

