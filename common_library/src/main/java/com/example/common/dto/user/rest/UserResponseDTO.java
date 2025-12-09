package com.example.common.dto.user.rest;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserResponseDTO {

    @Schema(description = "ID пользователя", example = "123")
    private Long id;

    @Schema(description = "Никнейм пользователя", example = "john_doe")
    private String nickName;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Фамилия пользователя", example = "Ivan")
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Ivanov")
    private String lastName;

    @Schema(description = "Имя аватарки пользователя", example = "...")
    private String avatarFileName;

    @Schema(description = "Имя аватарки пользователя", example = "г. Иркутск, ул. Москвовская, д.135")
    private String fullAddress;

}

