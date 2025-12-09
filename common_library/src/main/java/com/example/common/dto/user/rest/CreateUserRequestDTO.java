package com.example.common.dto.user.rest;

import com.example.common.security.XssSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Schema(description = "Запрос на создание пользователя")
public class CreateUserRequestDTO {

    @NotBlank(message = "Никнейм должен быть заполнен!")
    @Size(min=3, max=30, message = "Никнейм должен иметь длину от 3 до 30 символов")
    @Schema(description = "Никнейм пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "john_doe", minLength = 3, maxLength = 30)
    private String nickName;

    @NotBlank(message = "Имя должно быть заполнено!")
    @Size(min=3, max=30, message = "Имя должно иметь длину от 3 до 30 символов")
    @Schema(description = "Имя пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "Ivan", minLength = 3, maxLength = 30)
    private String firstName;


    @NotBlank(message = "Фамилия должна быть заполнена!")
    @Size(min=3, max=30, message = "Фамилия должна иметь длину от 3 до 30 символов")
    @Schema(description = "Фамилия пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "Ivanov", minLength = 3, maxLength = 30)
    private String lastName;

    @NotBlank(message = "Email должен быть заполнен!")
    @Email(message = "Вы ввели некорректный email")
    @Schema(description = "Email пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String email;

    @Setter
    @NotBlank(message = "Пароль должен быть заполнен!")
    @Size(min=8, max=40, message = "Пароль должен иметь длину от 8 до 40 символов")
    @Schema(description = "Пароль", requiredMode = Schema.RequiredMode.REQUIRED, example = "password12345", minLength = 8, maxLength = 40)
    private String password;

    @Setter
    @NotBlank(message = "Вы обязательно должны написать новый пароль второй раз!")
    @Size(min=8, max=40)
    @Schema(description = "Повтор пароля", requiredMode = Schema.RequiredMode.REQUIRED, example = "password12345", minLength = 8, maxLength = 40)
    private String repeatedPassword;

    public CreateUserRequestDTO(String nickName, String firstName, String lastName, String email, String password, String repeatedPassword) {
        this.nickName = XssSanitizer.sanitize(nickName);
        this.firstName = XssSanitizer.sanitize(firstName);
        this.lastName = XssSanitizer.sanitize(lastName);
        this.email = XssSanitizer.sanitize(email);
        this.password = password;
        this.repeatedPassword = repeatedPassword;
    }

}
