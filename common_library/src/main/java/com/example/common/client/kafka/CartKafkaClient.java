package com.example.common.client.kafka;

import com.example.common.dto.cart.kafka.AddItemsToCartDto;
import com.example.common.dto.cart.kafka.DeleteCartItemsDto;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.dto.product.kafka.ProductDeleteImageDto;
import com.example.common.dto.product.kafka.ProductSetImageDto;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.common.constant.kafka.Topics.CART_REQUEST_TOPIC;
import static com.example.common.constant.kafka.Topics.PRODUCT_REQUEST_TOPIC;
import static com.example.common.constant.kafka.keys.CartKeys.ADD_ITEMS_TO_CART;
import static com.example.common.constant.kafka.keys.CartKeys.DELETE_CART_ITEMS;
import static com.example.common.constant.kafka.keys.ProductKeys.DELETE_PRODUCT_IMAGE;
import static com.example.common.constant.kafka.keys.ProductKeys.SET_PRODUCT_IMAGE;

@Service
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class CartKafkaClient {

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @Lazy
    public void setKafkaTemplate(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void addItemsToCart(long userId, @NonNull List<ProductIdAndQuantityDto> itemsDTOs) {
        AddItemsToCartDto addItemsToCartDto = new AddItemsToCartDto();
        addItemsToCartDto.setUserId(userId);
        addItemsToCartDto.setItemDTOs(itemsDTOs);

        kafkaTemplate.send(CART_REQUEST_TOPIC, ADD_ITEMS_TO_CART, addItemsToCartDto);

    }

    public void deleteCartItems(long userId, @NonNull List<Long> productIds) {
        DeleteCartItemsDto deleteCartItemsDto = new DeleteCartItemsDto();
        deleteCartItemsDto.setUserId(userId);
        deleteCartItemsDto.setProductIDs(productIds);

        kafkaTemplate.send(CART_REQUEST_TOPIC, DELETE_CART_ITEMS, deleteCartItemsDto);

    }


}
