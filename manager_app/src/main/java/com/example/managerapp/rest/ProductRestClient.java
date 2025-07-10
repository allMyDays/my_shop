package com.example.managerapp.rest;

import com.example.managerapp.controller.payload.NewProductPayload;
import com.example.managerapp.entity.ProductRecord;
import com.example.managerapp.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProductRestClient {

    private final RestClient noAuthRestClient;
    private final RestClient withAuthRestClient;

    private static final ParameterizedTypeReference<List<ProductRecord>> PRODUCT_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    public ProductRestClient(@Qualifier("noAuth") RestClient noAuthRestClient,
                             @Qualifier("withAuth") RestClient withAuthRestClient) {
        this.noAuthRestClient = noAuthRestClient;
        this.withAuthRestClient = withAuthRestClient;
    }


    public List<ProductRecord> getAllProducts(Long categoryId, String filter) {
        return noAuthRestClient
                .get()
                .uri("/catalogue-api/products?categoryId={categoryId}&filter={filter}", categoryId,filter)
                .retrieve()
                .body(PRODUCT_TYPE_REFERENCE);


    }

    public Optional<ProductRecord> getProductByID(Long productID) {
        try {
            return Optional.ofNullable(
                    noAuthRestClient.get()
                            .uri("/catalogue-api/products/{productId}", productID)
                            .retrieve()
                            .body(ProductRecord.class));
        }catch (HttpClientErrorException.NotFound exception){
            return Optional.empty();


        }
    }
    public List<ProductRecord> getProductsByIDs(List<Long> productIDs) {
        try {
            return noAuthRestClient.post()
                    .uri("/catalogue-api/products/get-by-ids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(productIDs)
                    .retrieve()
                    .body(PRODUCT_TYPE_REFERENCE);
        }catch (HttpClientErrorException.NotFound exception){
            return List.of();


        }
    }

    public ProductRecord createProduct(NewProductPayload product, MultipartFile file1, MultipartFile file2, MultipartFile file3) {
        try {
            return withAuthRestClient
                    .post()
                    .uri("/catalogue-api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(product)
                    .retrieve()
                    .body(ProductRecord.class);
        }catch (HttpClientErrorException.BadRequest exception){
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException(problemDetail);



        }

    }

    public void updateProduct(Long productID, ProductRecord productRecord, MultipartFile file1, MultipartFile file2, MultipartFile file3) {
        try {
            withAuthRestClient
                    .patch()
                    .uri("/catalogue-api/products/{productID}", productID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(productRecord)
                    .retrieve()
                    .toBodilessEntity();
        }catch (HttpClientErrorException.BadRequest exception){
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException(problemDetail);

        }
    }

    public void deleteProduct(long productId) {
        try {
            withAuthRestClient.delete()
                    .uri("/catalogue-api/products/{productId}", productId)
                    .retrieve()
                    .toBodilessEntity();
        }catch (HttpClientErrorException.NotFound exception){
            throw new NoSuchElementException(exception);



        }
    }
























}
