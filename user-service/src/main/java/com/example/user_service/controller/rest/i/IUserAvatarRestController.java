package com.example.user_service.controller.rest.i;

import com.example.common.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "Аватар пользователя", description = "API для управления аватаром пользователя")
@SecurityRequirement(name = "JWT")
public interface IUserAvatarRestController {

    @Operation(
            summary = "Загрузить аватар пользователя",
            description = "Загружает или обновляет аватар текущего аутентифицированного пользователя.<br><br>" +
                    "<b>Ограничения файла:</b><br>" +
                    "• Максимальный размер: 2MB<br>" +
                    "• Рекомендуемые форматы: JPG, PNG" +
                    "• Рекомендуемое разрешение: 200x200 до 1024x1024 пикселей"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Аватар успешно загружен"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка при попытке установить аватар (причина ошибки будет отображена)"
            )
    })
    ResponseEntity<Void> uploadUserAvatar(
            @Parameter(
                    description = "Файл аватара",
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data"
                    )
            ) MultipartFile file,
            @Parameter(hidden = true) Jwt jwt
    ) throws UserNotFoundException;



    @Operation(
            summary = "Удалить аватар пользователя",
            description = "Удаляет аватар текущего аутентифицированного пользователя. " +
                    "После удаления будет установлен аватар по умолчанию."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Аватар успешно удален"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка при попытке удалить аватар (причина ошибки будет отображена)"
            )
    }) ResponseEntity<Void> deleteUserAvatar(
            @Parameter(hidden = true) Jwt jwt
    ) throws UserNotFoundException;
}