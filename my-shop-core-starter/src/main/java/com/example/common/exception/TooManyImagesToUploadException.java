package com.example.common.exception;

import jakarta.validation.constraints.Positive;
import lombok.NonNull;

public class TooManyImagesToUploadException extends RuntimeException{

    public TooManyImagesToUploadException(@NonNull @Positive Integer maxQuantity) {

        super("You are trying to upload too many images, maximal limit is " + maxQuantity);


    }


}
