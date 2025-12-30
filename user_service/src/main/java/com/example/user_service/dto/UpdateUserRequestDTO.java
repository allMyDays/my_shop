package com.example.user_service.dto;

import com.example.common.security.XssSanitizer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class UpdateUserRequestDTO {

    @Size(min=3, max=30, message = "Имя должно иметь длину от 3 до 30 символов")
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ-]+$", message = "Имя может содержать только буквы и дефис.")
    private String firstName;

    @Size(min=3, max=30, message = "Фамилия должна иметь длину от 3 до 30 символов")
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ-]+$", message = "Фамилия может содержать только буквы и дефис.")
    private String lastName;

    @Email(message = "Вы ввели некорректный email")
    private String email;

}
