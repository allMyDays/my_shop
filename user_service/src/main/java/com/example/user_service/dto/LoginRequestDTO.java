package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginRequestDTO {
    @NotBlank(message = "Никнейм не может быть пустым.")
    private String nickName;

    @NotBlank(message = "Пароль не может быть пустым.")
    private String password;


}
