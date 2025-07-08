package com.example.managerapp.dto;

import lombok.Data;

@Data
public class CartItemDTO {

    private Long id;

    private Long productId;

    private int quantity;

    String title;

    int price;

    String previewImageFileName;




}
