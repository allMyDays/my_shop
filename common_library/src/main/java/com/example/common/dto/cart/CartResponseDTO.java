package com.example.common.dto.cart;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private int totalQuantity;

    private List<CartItemResponseDTO> itemsDTOList = new ArrayList<>();


}
