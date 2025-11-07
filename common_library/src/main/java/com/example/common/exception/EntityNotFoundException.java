package com.example.common.exception;

import jakarta.validation.constraints.Positive;
import lombok.NonNull;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(@NonNull Class<?> classObj, @NonNull @Positive Long entityId) {
        super("Entity %s with id %d not found".formatted(classObj.getSimpleName(),entityId));




    }


}
