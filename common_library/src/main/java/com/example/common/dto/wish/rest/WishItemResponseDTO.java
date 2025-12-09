package com.example.common.dto.wish.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Полная информация о товаре из списка желаний")
public class WishItemResponseDTO {

    @Schema(description = "ID товара")
    private Long productId;

    @Schema(description = "Название товара")
    String title;

    @Schema(description = "Форматированная цена, готовая для отображения юзеру")
    String priceView;

    @Schema(description = "Имя аватарки товара. Нужно получить фото по имени через контроллер для изображений.")
    String previewImageFileName;


}
