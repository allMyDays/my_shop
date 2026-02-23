package com.example.common.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("We could not find correct user to complete the process");
    }

}
