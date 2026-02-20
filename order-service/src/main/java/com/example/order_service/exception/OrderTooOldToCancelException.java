package com.example.order_service.exception;

public class OrderTooOldToCancelException extends RuntimeException {

    public OrderTooOldToCancelException() {
        super("К сожалению, данный заказ не может быть отменен, так как он был создан более часа назад.");
    }


}
