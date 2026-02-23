package com.example.cart_wishlist.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Полная информация о списке желаний пользователя")
public class WishListResponseDTO {

    @Schema(description = "полное количество товаров в списке")
    private long totalQuantity;

    @Schema(description = "товары")
    private List<WishItemResponseDTO> itemsDTOList = new ArrayList<>();

}
