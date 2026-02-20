package com.example.user_service.exception;

public class KeycloakException extends RuntimeException{
    public KeycloakException(Throwable e){
        super(e);
    }
}
