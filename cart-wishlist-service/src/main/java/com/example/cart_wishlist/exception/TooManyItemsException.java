package com.example.cart_wishlist.exception;

public class TooManyItemsException extends RuntimeException{

    boolean cart;

    public TooManyItemsException(boolean cart){
        super("Вы превысили лимит на добавление товаров в %s. Удалите часть товаров, чтобы освободить место.".formatted(cart?"корзину":"список желаний"));
        this.cart = cart;
    }

    public boolean isCart() {
        return cart;
    }
}
