package com.example.managerapp.record;

import java.util.List;

public record Bucket (int id, MyUser user, List<Product> products) {
}
