package com.example.catalogue_service.init;


import com.example.catalogue_service.dto.DefaultProductDTO;
import com.example.catalogue_service.entity.Category;
import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.repository.CategoryRepository;
import com.example.catalogue_service.repository.ProductRepository;
import com.example.catalogue_service.service.ProductInitService;
import com.example.common.client.grpc.MediaGrpcClient;
import com.example.common.dto.media.kafka.PhotoDataDTO;
import com.example.common.enumeration.category.CategoryCode;
import com.example.common.enumeration.media.BucketEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    private final ProductInitService productInitService;


    @Override
    public void run(ApplicationArguments args) throws Exception {

         for(CategoryCode categoryCode : CategoryCode.values()) {     // сохраняю категории в базу данных
            if(!categoryRepository.existsByCode(categoryCode)){
                categoryRepository.save(new Category(categoryCode.getName(), categoryCode));
            }

        }
        try{

         productInitService.initDefaultProducts();                                                            // заполняю магазин дефолтными товарами

        }catch (Exception e){
            e.printStackTrace();
        }


    }
}

