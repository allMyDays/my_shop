package com.example.catalogue_service.controller.kafka;

import com.example.catalogue_service.service.ProductService;
import com.example.common.kafka.dto.product.ProductDeleteImageDto;
import com.example.common.kafka.dto.product.ProductSetImageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.example.common.kafka.constant.Topics.PRODUCT_TOPIC;
import static com.example.common.kafka.constant.keys.ProductKeys.DELETE_PRODUCT_IMAGE;
import static com.example.common.kafka.constant.keys.ProductKeys.SET_PRODUCT_IMAGE;

@Service
@RequiredArgsConstructor
public class ProductRequestsConsumer {

    private final ProductService productService;

    private final ObjectMapper objectMapper;


    @KafkaListener(topics = PRODUCT_TOPIC , groupId = "${spring.kafka.consumer.group-id}")
    public void handleProductsRequests(ConsumerRecord<String, String> record){

        String key = record.key();
        String rawJsonString = record.value();

        try{
         switch (key){
            case SET_PRODUCT_IMAGE:
                var setImageDto = objectMapper.readValue(rawJsonString, ProductSetImageDto.class);
                productService.setProductImage(setImageDto.getProductId(), setImageDto.getNewImageFileName(),setImageDto.isPreviewImage());
                break;
             case DELETE_PRODUCT_IMAGE:
                 var deleteImageDto = objectMapper.readValue(rawJsonString, ProductDeleteImageDto.class);
                 productService.deleteProductImage(deleteImageDto.getProductId(), deleteImageDto.getImageFileName(),deleteImageDto.isPreviewImage());
                 break;
         }
        }
     catch (Exception e){
            //todo

     }

    }
}
