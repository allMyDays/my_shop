package com.example.common.dto.cart.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponseDTO {

    private Long id;

    private Long productId;

    private int quantity;

    String title;

    String totalPriceView;

    int pricePerProductInt;

    String previewImageFileName;
}
