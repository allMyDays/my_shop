package com.example.order_service.dto;

import com.example.order_service.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderItemResponseDto {

    private Long productId;

    private Integer productQuantity;

    private String title;

    private int priceInt;

    private String priceView;

    private String previewImageFileName;


}
