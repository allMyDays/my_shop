package com.example.order_service.exception;

public class AddressNotCorrectException extends RuntimeException {

    public AddressNotCorrectException() {
        super("Введите существующий адрес с городом, улицей и домом!");


    }





}
