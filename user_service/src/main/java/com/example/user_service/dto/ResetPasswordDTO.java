package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.kafka.common.protocol.types.Field;

@Data
public class ResetPasswordDTO {

    @NotBlank(message = "Никнейм должен быть заполнен!")
    private String nickName;

    @NotBlank(message = "Email должен быть заполнен!")
    @Email(message = "Email введен некорректно!")
    private String email;

    @NotBlank(message = "Пароль должен быть заполнен!")
    @Size(min=8, max=40, message = "Пароль должен иметь длину от 8 до 40 символов")
    private String newPassword;

    @NotBlank(message = "Вы обязательно должны написать новый пароль второй раз!")
    @Size(min=8, max=40)
    private String repeatedPassword;







}
