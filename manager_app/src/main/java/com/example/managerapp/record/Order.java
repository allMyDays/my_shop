package com.example.managerapp.record;

import com.example.managerapp.record.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record Order (Long id, LocalDateTime created, LocalDateTime updated, MyUser user, int amount, String address, List<OrderDetails> details, OrderStatus status ) {
}
