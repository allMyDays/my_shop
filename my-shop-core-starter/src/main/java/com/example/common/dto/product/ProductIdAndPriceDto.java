package com.example.common.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Schema(description = "Сущность, представляющая собой ID товара и цену этого товара")
public class ProductIdAndPriceDto {

    private Long productId;

    private Integer productPrice;
}
