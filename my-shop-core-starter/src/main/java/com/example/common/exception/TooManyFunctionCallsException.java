package com.example.common.exception;

public class TooManyFunctionCallsException extends RuntimeException{

    public TooManyFunctionCallsException() {
        super("You temporarily exhausted the limit of this function calls");
    }



}
