package com.example.support_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Данные для создания чата с поддержкой")
public class CreateChatDto {

    @Size(min = 4, max = 30)
    @Schema(
           description = "Тема чата",
           requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Пробоема с оплатой заказа #123",
            minLength = 4,
            maxLength = 30
    )
    private String topic;


}
