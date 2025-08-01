package com.example.catalogue_service.repository;


import com.example.catalogue_service.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest   // указывает, что нужно поднять только jpa часть приложения и использует встроенную бд
@Sql("/sql/products.sql")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)     // указывает, что спринг не должен заменить нашу бд на встроенную
class ProductRepositoryIT {

    @Autowired
    ProductRepository productRepository;

    @Test
    void findAllByTitleLikeIgnoreCase_ReturnsFilteredProductList() {
        // given
        var filter = "%ood1%"; Long l = 1l;

        // when
       // var products = productRepository.findAllByTitleLikeIgnoreCase(filter);

       // assertEquals(List.of(new Product(1L, "good1", "desc 1", 0,null,null,null,null)),products);




    }





}