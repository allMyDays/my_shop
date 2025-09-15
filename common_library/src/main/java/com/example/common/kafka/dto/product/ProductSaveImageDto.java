package com.example.common.kafka.dto.product;

import lombok.Data;

@Data
public class ProductSaveImageDto {

    private Long creatorId;

    private Long productId;

    private String newImageFileName;

    private boolean preview;



}
