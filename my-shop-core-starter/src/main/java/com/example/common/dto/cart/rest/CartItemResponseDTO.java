package com.example.common.dto.cart.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Полная информация о товаре из корзины пользователя")
public class CartItemResponseDTO {

    @Schema(description = "Id сущности")
    private Long id;

    @Schema(description = " Id товара")
    private Long productId;

    @Schema(description = "Количество товара")
    private int quantity;

    @Schema(description = "Название товара")
    String title;

    @Schema(description = "Форматированная общая цена(с учетом количества товара), готовая для отображения юзеру")
    String totalPriceView;

    @Schema(description = "Цена товара за единицу")
    int pricePerProductInt;

    @Schema(description = "Имя аватарки товара. Нужно получить фото по имени через контроллер для изображений.")
    String previewImageFileName;
}
