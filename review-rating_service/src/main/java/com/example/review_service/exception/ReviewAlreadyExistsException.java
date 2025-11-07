package com.example.review_service.exception;

import jakarta.validation.constraints.Positive;
import lombok.NonNull;

public class ReviewAlreadyExistsException extends RuntimeException {

    public ReviewAlreadyExistsException(
            @NonNull @Positive Long userId,
            @NonNull @Positive Long productId) {

        super("Отзыв пользоваля с id %d уже существует у продукта с id %d".formatted(userId, productId));


    }


}
