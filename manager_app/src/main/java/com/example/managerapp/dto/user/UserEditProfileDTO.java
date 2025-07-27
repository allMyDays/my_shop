package com.example.managerapp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserEditProfileDTO {

    private String nickName;

    @Size(min=3, max=30, message = "Имя должно иметь длину от 3 до 30 символов")
    private String firstName;

    @Size(min=3, max=30, message = "Фамилия должна иметь длину от 3 до 30 символов")
    private String lastName;

    @Email(message = "Вы ввели некорректный email")
    private String email;

    @Size(min=8, max=40, message = "Пароль должен иметь длину от 8 до 40 символов")
    private String password;

    @Size(min=8, max=40, message = "Повторный пароль должен иметь длину от 8 до 40 символов")
    private String repeatedPassword;









}
