package com.example.cart_wishlist.controller.kafka;
import com.example.cart_wishlist.service.CartService;
import com.example.common.dto.cart.kafka.AddItemsToCartDto;
import com.example.common.dto.cart.kafka.DeleteCartItemsDto;
import com.example.common.dto.product.kafka.ProductDeleteImageDto;
import com.example.common.dto.product.kafka.ProductSetImageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.example.common.constant.kafka.Topics.CART_REQUEST_TOPIC;
import static com.example.common.constant.kafka.Topics.PRODUCT_REQUEST_TOPIC;
import static com.example.common.constant.kafka.keys.CartKeys.ADD_ITEMS_TO_CART;
import static com.example.common.constant.kafka.keys.CartKeys.DELETE_CART_ITEMS;
import static com.example.common.constant.kafka.keys.ProductKeys.DELETE_PRODUCT_IMAGE;
import static com.example.common.constant.kafka.keys.ProductKeys.SET_PRODUCT_IMAGE;

@Service
@RequiredArgsConstructor
public class CartRequestsConsumer {

    private final ObjectMapper objectMapper;

    private final CartService cartService;


    @KafkaListener(topics = CART_REQUEST_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleProductsRequests(ConsumerRecord<String, String> record){

        String key = record.key();
        String rawJsonString = record.value();

        try{
         switch (key){
            case ADD_ITEMS_TO_CART:
                var addItemDto = objectMapper.readValue(rawJsonString, AddItemsToCartDto.class);
                cartService.addItemsToCart(addItemDto.getUserId(), addItemDto.getItemDTOs());

                break;
             case DELETE_CART_ITEMS:
                 var deleteItemDto = objectMapper.readValue(rawJsonString, DeleteCartItemsDto.class);
                 cartService.removeItemsFromCart(deleteItemDto.getUserId(),deleteItemDto.getProductIDs());
                 break;

         }
        }
     catch (Exception e){
            //todo

     }

    }
}
