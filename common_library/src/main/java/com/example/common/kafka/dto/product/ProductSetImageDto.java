package com.example.common.kafka.dto.product;

import lombok.Data;

@Data
public class ProductSetImageDto {

    private Long productId;

    private String newImageFileName;

    private boolean previewImage;



}
