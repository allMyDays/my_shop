package com.example.managerapp.controller;

import com.example.managerapp.entity.ProductRecord;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import wiremock.org.apache.hc.core5.http.HttpHeaders;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
public class ProductsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getNewProductPage_ReturnsProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/product_create").with(user("ivan").roles("CUSTOMER"));

        // when

        mockMvc.perform(requestBuilder)

        // then
                .andDo(print())
                .andExpectAll(
                     status().isOk(),
                     view().name("catalogue/products/new_product")


                );

    }
    @Test
    void getProductList_ReturnsProductListPage() throws Exception {
        // given

        var requestBuilder = MockMvcRequestBuilders.get("/products")
                .queryParam("filter", "товар")
                .with(user("ivan").roles("CUSTOMER"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар"))
                .willReturn(WireMock.ok("""
                        [
                            {"id": 1, "title": "Товар №1", "description": "Описание товара №1", "price": 0, "previewImageID": null},
                            {"id": 2, "title": "Товар №2", "description": "Описание товара №2", "price": 0, "previewImageID": null}
                        ]""").withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));


        // when

        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                       status().isOk(),
                        view().name("products"),
                       // model().attribute("title",title);
                        model().attribute("products", List.of(
                                new ProductRecord(1,"Товар №1",0,"Описание товара №1",null,null),
                                new ProductRecord(2,"Товар №2",0,"Описание товара №2",null,null)

                        ))
                );
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар"))
        );


    }







}
