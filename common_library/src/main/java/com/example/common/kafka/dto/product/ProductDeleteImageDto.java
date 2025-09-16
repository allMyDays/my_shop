package com.example.common.kafka.dto.product;

import lombok.Data;

@Data
public class ProductDeleteImageDto {

    private Long productId;

    private String imageFileName;

    private boolean previewImage;



}
