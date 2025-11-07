package com.example.order_service.exception;

import jakarta.validation.constraints.Positive;
import lombok.NonNull;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(@NonNull @Positive Long userId) {
        super("this function requires an user living address that was not found for user with id " + userId);
    }



}
