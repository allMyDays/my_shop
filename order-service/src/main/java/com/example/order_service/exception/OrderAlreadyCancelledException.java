package com.example.order_service.exception;

public class OrderAlreadyCancelledException extends RuntimeException {

    public OrderAlreadyCancelledException() {
        super("Данный заказ уже был отменен, повторная отмена не требуется.");
    }



}
