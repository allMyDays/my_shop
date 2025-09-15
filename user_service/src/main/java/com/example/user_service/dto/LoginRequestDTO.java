package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginRequestDTO {
    @NotBlank(message = "nickName cannot be blank")
    private String nickName;

    @NotBlank(message = "password cannot be blank")
    private String password;


}
