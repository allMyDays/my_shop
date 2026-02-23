package com.example.common.dto.product.kafka;

import lombok.Data;

@Data
public class ProductSetImageDto {

    private Long productId;

    private String newImageFileName;

    private boolean previewImage;



}
