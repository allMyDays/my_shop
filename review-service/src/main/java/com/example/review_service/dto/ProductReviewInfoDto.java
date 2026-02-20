package com.example.review_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductReviewInfoDto {

    private Long productId;

    private Long reviewQuantity;

    private double averageRating;

}
