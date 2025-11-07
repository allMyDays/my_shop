package com.example.common.dto.cart.kafka;

import com.example.common.dto.product.ProductIdAndQuantityDto;
import lombok.Data;

import java.util.List;

@Data
public class AddItemsToCartDto {

    private Long userId;

    private List<ProductIdAndQuantityDto> itemDTOs;


}
