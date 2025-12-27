package com.example.support_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CreateSupportMessageRequestDto {

    @NotNull
    @Positive(message = "chatId must be positive")
    @Schema(description = "ID чата", example = "123")
    private Long chatId;

    @Schema(description = "Текстовое сообщение", example = "Здравствуйте!")
    private String message;







}
