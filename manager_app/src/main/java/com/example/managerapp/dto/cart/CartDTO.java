package com.example.managerapp.dto.cart;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CartDTO {

    private int totalQuantity;

    private List<CartItemDTO> itemsDTOList = new ArrayList<>();







}
