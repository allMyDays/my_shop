package com.example.catalogue_service.controller.kafka;

import com.example.catalogue_service.service.ProductService;
import com.example.common.kafka.dto.product.ProductSaveImageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import static com.example.common.kafka.constant.Topics.PRODUCT_TOPIC;
import static com.example.common.kafka.constant.keys.ProductKeys.SAVE_PRODUCT_IMAGE;

@Service
@RequiredArgsConstructor
public class ProductRequestsConsumer {

    private final ProductService productService;

    private final ObjectMapper objectMapper;


    @KafkaListener(topics = PRODUCT_TOPIC , groupId = "${spring.kafka.consumer.group-id}")
    public void handleProductsRequests(
            @Payload String rawJsonString,
            @Header(KafkaHeaders.RECEIVED_KEY) String key){

        try{
         switch (key){
            case SAVE_PRODUCT_IMAGE:
                var saveDto = objectMapper.readValue(rawJsonString, ProductSaveImageDto.class);
                //productService.
                break;


             default:


          }
        }
     catch (Exception e){
            //todo

     }

    }
}
