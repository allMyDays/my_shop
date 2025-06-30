package com.example.managerapp.entity;

import java.util.List;

public record Bucket (int id, MyUser user, List<Product> products) {
}
