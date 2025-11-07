package com.example.common.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
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
