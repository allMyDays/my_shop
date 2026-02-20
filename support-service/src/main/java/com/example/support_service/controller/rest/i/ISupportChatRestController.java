package com.example.support_service.controller.rest.i;

import com.example.common.dto.support.SupportChatResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.support_service.dto.CreateChatDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;

import java.util.List;

@Tag(name = "Поддержка: Чаты", description = "API для управления чатами с техподдержкой")
@SecurityRequirement(name = "JWT") // Требует JWT токен для всех методов
public interface ISupportChatRestController {


        @Operation(
                summary = "Создать чат с поддержкой",
                description = "Создает новый чат с техподдержкой по указанной теме"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Чат успешно создан",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = java.util.Map.class),
                                examples = @ExampleObject(
                                        name = "Успешный ответ",
                                        value = """
                        {
                          "chatId": 123,
                          "userId": 456
                        }
                        """
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Ошибки валидации или иные ошибки при создании чата",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(type = "array",
                                        example = "[\"Тема должна содержать от 4 до 30 символов\"]")
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                )
        })
         ResponseEntity<?> createSupportChat(@Parameter(hidden = true) Jwt jwt,

                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Данные для создания чата",
                        required = true,
                        content = @Content(schema = @Schema(implementation = CreateChatDto.class))
                ) CreateChatDto createChatDto,

                @Parameter(hidden = true) BindingResult bindingResult) throws UserNotFoundException;

        @Operation(
                summary = "Проверить возможность создания чата",
                description = "Проверяет, может ли пользователь создать новый чат с поддержкой (не превышен ли лимит)"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Можно создавать чат"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                ),
                @ApiResponse(
                        responseCode = "429",
                        description = "Превышен лимит создания чатов",
                        content = @Content(mediaType = "text/plain",
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject(
                                        value = "You temporarily exhausted the limit of creation support chats"
                                )
                        )
                )
        })
        ResponseEntity<?> checkChatCreationAbility(@Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;


        @Operation(
                summary = "Удалить чат поддержки",
                description = "Удаляет чат с техподдержкой по его ID"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Чат успешно удален"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Ошибки при удалении (например, чат не найден или не принадлежит пользователю)"
                )
        })
        ResponseEntity<?> deleteSupportChat(
                @Parameter(hidden = true) Jwt jwt,

                @Parameter(
                        description = "ID чата для удаления",
                        required = true,
                        example = "123"
                ) Long chatId) throws UserNotFoundException;


        @Operation(
                summary = "Получить все чаты пользователя",
                description = "Возвращает список всех чатов с поддержкой для текущего пользователя"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Список чатов успешно получен",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = SupportChatResponseDTO.class, type = "array"))
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                )
        })
        List<SupportChatResponseDTO> getAllUserSupportChats(@Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;
    }











