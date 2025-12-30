package com.example.support_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Данные для создания чата с поддержкой")
public class CreateChatDto {

    @NotBlank(message = "Тема чата не может быть пустой.")
    @Size(min = 4, max = 30, message = "Тема чата должна быть длиной от 4 до 30 символов.")
    @Pattern(
            regexp = "^[a-zA-Zа-яА-ЯёЁ0-9 .,!?():;№#_\\-]+$",
            message = "В теме чата были обнаружены запрещенные символы."
    )
    @Schema(
           description = "Тема чата",
           requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Проблема с оплатой заказа #123",
            minLength = 4,
            maxLength = 30
    )
    private String topic;


}
