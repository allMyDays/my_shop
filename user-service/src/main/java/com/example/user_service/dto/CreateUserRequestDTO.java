package com.example.user_service.dto;

import com.example.common.security.XssSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Schema(description = "Запрос на создание пользователя")
@AllArgsConstructor
@Setter
public class CreateUserRequestDTO {

    @NotBlank(message = "Никнейм должен быть заполнен!")
    @Size(min=4, max=30, message = "Никнейм должен иметь длину от 4 до 30 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "Никнейм может содержать только латинские буквы, цифры, а также символы _ и .")
    @Schema(description = "Никнейм пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "john_doe", minLength = 3, maxLength = 30)
    private String nickName;

    @NotBlank(message = "Имя должно быть заполнено!")
    @Size(min=3, max=30, message = "Имя должно иметь длину от 3 до 30 символов")
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ-]+$", message = "Имя может содержать только буквы и дефис.")
    @Schema(description = "Имя пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "Ivan", minLength = 3, maxLength = 30)
    private String firstName;


    @NotBlank(message = "Фамилия должна быть заполнена!")
    @Size(min=3, max=30, message = "Фамилия должна иметь длину от 3 до 30 символов")
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ-]+$", message = "Фамилия может содержать только буквы и дефис.")
    @Schema(description = "Фамилия пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "Ivanov", minLength = 3, maxLength = 30)
    private String lastName;

    @NotBlank(message = "Email должен быть заполнен!")
    @Email(message = "Вы ввели некорректный email")
    @Schema(description = "Email пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String email;

    @Setter
    @NotBlank(message = "Пароль должен быть заполнен!")
    @Size(min=8, max=40, message = "Пароль должен иметь длину от 8 до 40 символов")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "Пароль должен содержать буквы, цифры и спецсимвол"
    )
    @Schema(description = "Пароль", requiredMode = Schema.RequiredMode.REQUIRED, example = "password12345", minLength = 8, maxLength = 40)
    private String password;

    @Setter
    @NotBlank(message = "Вы обязательно должны написать свой новый пароль второй раз!")
    //@Size(min=8, max=40)
   // @Schema(description = "Повтор пароля", requiredMode = Schema.RequiredMode.REQUIRED, example = "password12345", minLength = 8, maxLength = 40)
    private String repeatedPassword;


}
