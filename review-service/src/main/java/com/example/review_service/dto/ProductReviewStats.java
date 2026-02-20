package com.example.review_service.dto;

public interface ProductReviewStats {
    Long getProductId();
    Long getReviewCount();
    Double getAverageRating();
}