package com.example.managerapp.controller;

import com.example.managerapp.controller.payload.NewProductPayload;
import com.example.managerapp.exception.BadRequestException;
import com.example.managerapp.entity.ProductRecord;
import com.example.managerapp.rest.ProductRestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductController")
class ProductRecordControllerTest {

    @Mock
    private ProductRestClient productRestClient;

    @InjectMocks
    ProductController controller;

    @Test
    @DisplayName("CreateProduct создаст новый товар и перенаправит на страницу товара")
    void createProduct_ValidRequest_ReturnsPageWithCreatedProduct() {

        // given
        var product = new NewProductPayload("new productRecord", 60, "simple description");
        var model = new ConcurrentModel();

        doReturn(new ProductRecord(1, "title", 60, "simple desc", null))
                .when(productRestClient)
                .createProduct(product,null,null,null);


        // when
        var result = controller.createProduct(product, model, null, null, null, null);

        assertEquals("redirect:/catalogue/products/1", result);

        verify(productRestClient).createProduct(product,null,null,null);

    }

    @Test
    @DisplayName("createProduct вернет страницу с ошибками, если запрос невалиден")
    void createProduct_InvalidRequest_ReturnsPageWithProductCreation() {
        // given
        var product = new NewProductPayload(null, -9, null);
        var model = new ConcurrentModel();

        doThrow(new BadRequestException(List.of("ошибка1","ошибка2")))
                .when(productRestClient)
                .createProduct(product,null,null,null);

        // when

        var result = controller.createProduct(product, model, null, null, null, null);

        assertEquals(result, "catalogue/products/new_product");










    }




}