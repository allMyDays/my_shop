package com.example.common.client.rest;

import com.example.common.dto.product.NewProductRequestDTO;
import com.example.common.dto.product.ProductResponseDTO;
import com.example.common.exception.BadRequestException;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
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
@ConditionalOnClass(RestClient.Builder.class)
@ConditionalOnBean(RestClient.Builder.class)
public class ProductRestClient {

    private RestClient noAuthRestClient;
    private RestClient withAuthRestClient;
    private static final ParameterizedTypeReference<List<ProductResponseDTO>> PRODUCT_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    @Autowired
    @Lazy
    public void setNoAuthRestClient( @Qualifier("noAuthCatalogueRestClient") RestClient noAuthRestClient) {
        this.noAuthRestClient = noAuthRestClient;
    }

    @Autowired
    @Lazy
    public void setWithAuthRestClient(@Qualifier("authCatalogueRestClient") RestClient withAuthRestClient) {
        this.withAuthRestClient = withAuthRestClient;
    }

    public List<ProductResponseDTO> getAllProducts(Long categoryId, String filter) {
        return noAuthRestClient
                .get()
                .uri("/api/products?categoryId={categoryId}&filter={filter}", categoryId,filter)
                .retrieve()
                .body(PRODUCT_TYPE_REFERENCE);
    }

    public Optional<ProductResponseDTO> getProductByID(Long productID) {
        try {
            return Optional.ofNullable(
                    noAuthRestClient.get()
                            .uri("/api/products/{productId}", productID)
                            .retrieve()
                            .body(ProductResponseDTO.class));
        }catch (HttpClientErrorException.NotFound exception){
            return Optional.empty();


        }
    }
    public List<ProductResponseDTO> getProductsByIDs(List<Long> productIDs) {
        try {
            return noAuthRestClient.post()
                    .uri("/api/products/get-by-ids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(productIDs)
                    .retrieve()
                    .body(PRODUCT_TYPE_REFERENCE);
        }catch (HttpClientErrorException.NotFound exception){
            return List.of();


        }
    }

    public ProductResponseDTO createProduct(NewProductRequestDTO product, MultipartFile file1, MultipartFile file2, MultipartFile file3) {
        try {
            return withAuthRestClient
                    .post()
                    .uri("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(product)
                    .retrieve()
                    .body(ProductResponseDTO.class);
        }catch (HttpClientErrorException.BadRequest exception){
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException(problemDetail);



        }

    }

    public void updateProduct(Long productID, ProductResponseDTO product, MultipartFile file1, MultipartFile file2, MultipartFile file3) {
        try {
            withAuthRestClient
                    .patch()
                    .uri("/api/products/{productID}", productID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(product)
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
                    .uri("/api/products/{productId}", productId)
                    .retrieve()
                    .toBodilessEntity();
        }catch (HttpClientErrorException.NotFound exception){
            throw new NoSuchElementException(exception);



        }
    }

}
