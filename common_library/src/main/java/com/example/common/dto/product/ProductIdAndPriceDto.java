package com.example.common.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ProductIdAndPriceDto {
    private Long productId;

    private Integer productPrice;
}
