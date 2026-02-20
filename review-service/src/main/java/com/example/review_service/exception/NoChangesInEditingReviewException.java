package com.example.review_service.exception;

public class NoChangesInEditingReviewException extends RuntimeException{
    public NoChangesInEditingReviewException(Long reviewId) {
        super("Отзыв с id %d уже имеет указанные параметры.".formatted(reviewId));


    }


}
