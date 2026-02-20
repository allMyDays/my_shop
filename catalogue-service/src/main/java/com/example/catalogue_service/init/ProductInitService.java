package com.example.catalogue_service.init;


import com.example.catalogue_service.dto.DefaultProductDTO;
import com.example.catalogue_service.entity.Category;
import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.repository.CategoryRepository;
import com.example.catalogue_service.repository.ProductRepository;
import com.example.common.client.grpc.MediaGrpcClient;
import com.example.common.dto.media.kafka.PhotoDataDTO;
import com.example.common.enumeration.media.BucketEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile("test")
@RequiredArgsConstructor
@Slf4j
@DependsOn("categoryInitService")
public class ProductInitService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final ResourceLoader resourceLoader;

    private final MediaGrpcClient mediaGrpcClient;

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void initProducts() {

        try {

            Map<String, Category> categoriesMap = categoryRepository.findAll().stream()
                    .collect(Collectors.toMap(c -> c.getCode().name(), Function.identity()));

            List<DefaultProductDTO> products = loadProducts();

            for (DefaultProductDTO dto : products) {
                if (productRepository.existsByIdentifyingCode(dto.getCode())) {
                    continue;
                }

                log.info("NOW SAVING DEFAULT PRODUCT:{} ", dto.getCode());
                Product product = new Product(
                        dto.getTitle(),
                        dto.getDescription(),
                        dto.getPrice(),
                        new ArrayList<>(List.of(categoriesMap.get(dto.getCategory().toUpperCase()))),
                        LocalDateTime.now());

                product.setIdentifyingCode(dto.getCode());

                List<String> images = uploadImages(dto);

                if (!images.isEmpty()) {
                    product.setPreviewImageFileName(images.get(0));
                    images.remove(0);
                    if (!images.isEmpty()) {
                        product.setImageFileNames(images);
                    }
                }


                productRepository.save(product);
                log.info("SUCCESSFULLY SAVED DEFAULT PRODUCT:{} ", dto.getCode());

            }

            log.info("Default products initialized: {}", products.size());

        }catch (Exception e){
            log.warn("Error while initialization default products: {}", e.getMessage());
            e.printStackTrace();
        }



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

    private List<String> uploadImages(DefaultProductDTO dto)  {

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

        for(int i=1; i<=20;i++){
            try{
                return byteList.isEmpty()?new ArrayList<>():new ArrayList<>(mediaGrpcClient.uploadPhotos(
                        byteList.stream()
                                .map(b-> new PhotoDataDTO(b, MediaType.IMAGE_PNG_VALUE))
                                .toList(),
                        BucketEnum.products));
            }catch (Exception e){
                log.warn("Exception while trying upload default product's photos. Attempt: {}, exception message: {}", i, e.getMessage());
                try {
                    Thread.sleep(60_000*i);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

            }
        }
        log.warn("Attempt limit exhausted while trying upload default product's photos.");
        throw new RuntimeException("Attempt limit exhausted while trying upload default product's photos.");
    }








}
