package com.example.managerapp.entity;

public record OrderDetails (int id, Order order, Product product, int price, int amount) {
}
