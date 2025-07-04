package com.example.managerapp.rest;

import com.example.managerapp.controller.payload.NewProductPayload;
import com.example.managerapp.entity.ProductRecord;
import com.example.managerapp.exception.BadRequestException;
import com.example.managerapp.entity.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
public class ProductRestClient {

   // private final UserRestClient userRestClient;
 //   private final BucketRestClient bucketRestClient;
    private final RestClient restClient;
    private static final ParameterizedTypeReference<List<ProductRecord>> PRODUCT_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};



    public List<ProductRecord> getAllProducts(String filter) {
        return restClient
                .get()
                .uri("/catalogue-api/products?filter={filter}", filter)
                .retrieve()
                .body(PRODUCT_TYPE_REFERENCE);


    }

    public ProductRecord createProduct(NewProductPayload product, MultipartFile file1, MultipartFile file2, MultipartFile file3) {
        try {
            return restClient
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

    public Optional<ProductRecord> getProductByID(Long productID) {
        try {
            return Optional.ofNullable(
                    restClient.get()
                            .uri("/catalogue-api/products/{productId}", productID)
                            .retrieve()
                            .body(ProductRecord.class));
        }catch (HttpClientErrorException.NotFound exception){
        return Optional.empty();


        }
    }
    public List<ProductRecord> getProductsByIDs(List<Long> productIDs) {
        try {
            return restClient.post()
                            .uri("/catalogue-api/products/get-by-ids")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(productIDs)
                            .retrieve()
                            .body(PRODUCT_TYPE_REFERENCE);
        }catch (HttpClientErrorException.NotFound exception){
            return List.of();


        }
    }






    public void updateProduct(Long productID, ProductRecord productRecord, MultipartFile file1, MultipartFile file2, MultipartFile file3) {
        try {
            restClient
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
            restClient.delete()
                    .uri("/catalogue-api/products/{productId}", productId)
                    .retrieve()
                    .toBodilessEntity();
        }catch (HttpClientErrorException.NotFound exception){
            throw new NoSuchElementException(exception);



        }
    }




    private Image toImage(MultipartFile file){
       /* Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        try {
            image.setBytes(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;*/ return null;

    }



}
