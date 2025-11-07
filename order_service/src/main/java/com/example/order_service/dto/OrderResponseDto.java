package com.example.order_service.dto;

import com.example.order_service.enumeration.OrderLivingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class OrderResponseDto {

    private Long id;

    private List<OrderItemResponseDto> itemDTOs;

    private LocalDateTime dateOfCreation;

    private OrderLivingStatus orderStatus;

    private String orderStatusView;

    private String address;

    private Integer commonItemQuantity;

    private Integer uniqueItemQuantity;

    private int totalPriceInt;

    private String totalPriceView;

    private int deliveryPriceInt;

    private String deliveryPriceView;

}
