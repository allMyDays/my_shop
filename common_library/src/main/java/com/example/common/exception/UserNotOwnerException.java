package com.example.common.exception;

import jakarta.validation.constraints.Positive;
import lombok.NonNull;

public class UserNotOwnerException extends RuntimeException {
    public UserNotOwnerException(
            @NonNull @Positive Long userId,
            @NonNull @Positive Long entityId,
            @NonNull Class<?> classObj) {
        super("User with id %d is not owner of entity %s with id %d, so this user cannot edit of delete it.".formatted(userId,classObj.getSimpleName(),entityId));

    }


}
