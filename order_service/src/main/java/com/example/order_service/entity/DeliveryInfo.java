package com.example.order_service.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;

@Entity
@Data
public class DeliveryInfo {

    @Id
    @EqualsAndHashCode.Include
    private Long userId;

    @Column(nullable = false)
    private String userAddress;

    @Column(nullable = false)
    private double addressLatitude;

    @Column(nullable = false)
    private double addressLongitude;

    @Column(nullable = false)
    private long deliveryTime;

    @Column(nullable = false)
    private long deliveryDistance;

}
