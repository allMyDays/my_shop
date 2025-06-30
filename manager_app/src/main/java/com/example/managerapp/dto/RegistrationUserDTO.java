package com.example.managerapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistrationUserDTO {

    @NotBlank(message = "Никнейм должен быть заполнен!")
    @Size(min=3, max=30, message = "Никнейм должен иметь длину от 3 до 30 символов")
    private String nickName;

    @NotBlank(message = "Имя должно быть заполнено!")
    @Size(min=3, max=30, message = "Имя должно иметь длину от 3 до 30 символов")
    private String firstName;


    @NotBlank(message = "Фамилия должна быть заполнена!")
    @Size(min=3, max=30, message = "Фамилия должна иметь длину от 3 до 30 символов")
    private String lastName;

    @NotBlank(message = "Email должен быть заполнен!")
    @Email(message = "Вы ввели некорректный email")
    private String email;

    @NotBlank(message = "Пароль должен быть заполнен!")
    @Size(min=8, max=40, message = "Пароль должен иметь длину от 8 до 40 символов")
    private String password;

    @NotBlank(message = "Вы обязательно должны написать новый пароль второй раз!")
    @Size(min=8, max=40)
    private String repeatedPassword;
}
