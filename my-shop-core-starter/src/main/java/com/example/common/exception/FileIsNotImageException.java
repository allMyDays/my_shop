package com.example.common.exception;


public class FileIsNotImageException extends RuntimeException{

    public FileIsNotImageException(String contentType) {
        super("You tried to upload a file that is not an image. Current file contentType: " + contentType);
    }

}
