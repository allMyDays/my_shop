package com.example.managerapp.record;

public record OrderDetails (int id, Order order, Product product, int price, int amount) {
}
