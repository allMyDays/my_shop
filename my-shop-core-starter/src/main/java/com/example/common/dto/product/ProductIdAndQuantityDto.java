package com.example.common.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@NoArgsConstructor
@Schema(description = "Сущность, представляющая собой ID товара и количество этого товара.")
public class ProductIdAndQuantityDto {

    private Long productId;

    private Integer productQuantity;


    public void setProductId(@Positive long productId) {
        this.productId = productId;
    }

    public void setProductQuantity(@Positive int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public ProductIdAndQuantityDto(@Positive long productId, @Positive int productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }
}
