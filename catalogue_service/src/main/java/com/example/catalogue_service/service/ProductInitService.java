package com.example.catalogue_service.service;

import com.example.catalogue_service.dto.DefaultProductDTO;
import com.example.catalogue_service.entity.Category;
import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.repository.CategoryRepository;
import com.example.catalogue_service.repository.ProductRepository;
import com.example.common.client.grpc.MediaGrpcClient;
import com.example.common.dto.media.kafka.PhotoDataDTO;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.enumeration.category.CategoryCode;
import com.example.common.enumeration.media.BucketEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInitService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final ResourceLoader resourceLoader;

    private final ObjectMapper objectMapper;

    private final MediaGrpcClient mediaGrpcClient;

    @Transactional
    public void initDefaultProducts() throws IOException {

        if(productRepository.count()>0){
            log.info("Products already exist");
            return;

        }

            Map<String, Category> categoriesMap = categoryRepository.findAll().stream()
                    .collect(Collectors.toMap(c->c.getCode().name(), Function.identity()));

         List<DefaultProductDTO> products = loadProducts();

         for (DefaultProductDTO dto : products) {
             System.out.println("NOW SAVING DEFAULT PRODUCT: "+dto.getCode());
            Product product = new Product(
                            dto.getTitle(),
                            dto.getDescription(),
                            dto.getPrice(),
                            new ArrayList<>(List.of(categoriesMap.get(dto.getCategory().toUpperCase()))),
                            LocalDateTime.now());

            List<String> images = uploadImages(dto);

            if(!images.isEmpty()){
                product.setPreviewImageFileName(images.get(0));
                images.remove(0);
                if(!images.isEmpty()){
                    product.setImageFileNames(images);
                }
            }


            productRepository.save(product);
             System.out.println("SUCCESSFULLY SAVED DEFAULT PRODUCT: "+dto.getCode());

        }

        log.info("Default products initialized: {}", products.size());
    }

private List<DefaultProductDTO> loadProducts() throws IOException {
    InputStream is = resourceLoader
            .getResource("classpath:default/products.json")
            .getInputStream();

    return objectMapper.readValue(
            is,
            new TypeReference<List<DefaultProductDTO>>() {}
    );
}

private List<String> uploadImages(DefaultProductDTO dto){

    List<String> imageNames = new ArrayList<>(dto.getImages());
    imageNames.add(0, dto.getAvatar());

    List<byte[]> byteList = imageNames.stream().map(img->
    {
        try {
            return resourceLoader
                    .getResource("classpath:default/images/"
                            + dto.getCategory().toLowerCase()+"/"
                            + dto.getCode() + "/" + img)
                    .getInputStream()
                    .readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }).toList();

    return byteList.isEmpty()?new ArrayList<>():new ArrayList<>(mediaGrpcClient.uploadPhotos(
            byteList.stream()
                    .map(b-> new PhotoDataDTO(b, MediaType.IMAGE_PNG_VALUE))
                    .toList(),
            BucketEnum.products));
  }
}
