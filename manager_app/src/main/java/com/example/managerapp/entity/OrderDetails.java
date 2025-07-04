package com.example.managerapp.entity;

public record OrderDetails (int id, Order order, ProductRecord productRecord, int price, int amount) {
}
