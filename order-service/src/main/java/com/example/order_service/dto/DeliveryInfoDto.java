package com.example.order_service.dto;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeliveryInfoDto {

    private Long userId;

    private String userAddress;

  //  private double addressLatitude;

 //   private double addressLongitude;

    private long deliveryTime;

    private long deliveryDistance;

    private long deliveryStraightDistance;

    private String storageAddress;

    private String deliveryTimeView;

    private String deliveryDistanceView;

    private String deliveryStraightDistanceView;

    private int deliveryPrice;

    private String deliveryPriceView;









}
