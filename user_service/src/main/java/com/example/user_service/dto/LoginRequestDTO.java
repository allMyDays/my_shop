package com.example.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "Учетные данные для входа")
public class LoginRequestDTO {
    @NotBlank(message = "Никнейм не может быть пустым.")
    @Schema(description = "Никнейм или email", required = true, example = "john_doe")
    private String nickName;

    @NotBlank(message = "Пароль не может быть пустым.")
    @Schema(description = "Пароль", required = true, example = "password1234")
    private String password;


}
