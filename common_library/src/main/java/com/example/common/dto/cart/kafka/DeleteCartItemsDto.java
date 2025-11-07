package com.example.common.dto.cart.kafka;

import lombok.Data;

import java.util.List;

@Data
public class DeleteCartItemsDto {

    private Long userId;

    private List<Long> productIDs;


}
