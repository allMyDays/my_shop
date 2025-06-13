package com.example.catalogue_service.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductsRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/products.sql")
    void findProduct_ReturnsProductsList() throws Exception {
        // given

        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products")
                .param("filter","good1")
                .with(jwt().jwt(builder -> builder.claim("scope","view_catalogue")));

        // when
        mockMvc.perform(requestBuilder)
        // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                           [
                              {"id": 1, "title": "good1", "description": "desc 1", "price": 0}
                           
  
                            ]"""));



    }

    @Test
    void createProduct_validResult_ReturnsCreatedProduct() throws Exception {

        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "good2", "description": "desc 2", "price": 0}
                        """)
                .with(jwt().jwt(builder -> builder.claim("scope","edit_catalogue")));

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/catalogue-api/products/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                           
                              {"id": 1, "title": "good2", "description": "desc 2", "price": 0}
                           
  
                            """));
    }

    @Test
    void createProduct_invalidResult_ReturnsProblemDetails() throws Exception {

        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": null, "description": "desc", "price": 0}
                        """)
                .with(jwt().jwt(builder -> builder.claim("scope","edit_catalogue")));

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                           {
                            "errors": [ "title must not be null" ]
                           }
                            """));
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {

        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": null, "description": "desc", "price": 0}
                        """)
                .with(jwt().jwt(builder -> builder.claim("scope","view_catalogue")));

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

}