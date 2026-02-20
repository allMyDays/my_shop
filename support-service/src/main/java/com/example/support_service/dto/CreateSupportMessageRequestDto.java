package com.example.support_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(
            regexp = "^[^<>]*$", message = "Сообщение содержит недопусимые символы."
    )
    private String message;







}
