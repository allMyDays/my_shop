package com.example.common.dto.cart.rest;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private long totalQuantity;

    private List<CartItemResponseDTO> itemsDTOList = new ArrayList<>();


}
