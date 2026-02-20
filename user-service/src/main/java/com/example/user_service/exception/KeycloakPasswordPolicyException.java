package com.example.user_service.exception;

public class KeycloakPasswordPolicyException extends RuntimeException{
    public KeycloakPasswordPolicyException(Throwable e){
        super(e);

    }
}
