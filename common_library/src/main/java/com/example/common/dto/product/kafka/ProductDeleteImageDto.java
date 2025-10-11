package com.example.common.dto.product.kafka;

import lombok.Data;

@Data
public class ProductDeleteImageDto {

    private Long productId;

    private String imageFileName;

    private boolean previewImage;



}
