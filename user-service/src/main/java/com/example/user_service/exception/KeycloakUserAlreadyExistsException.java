package com.example.user_service.exception;

public class KeycloakUserAlreadyExistsException extends RuntimeException{

    public KeycloakUserAlreadyExistsException(Throwable e){
        super(e);
    }
}
