package com.example.support_service.controller.rest.i;

import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.common.exception.UserNotFoundException;
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

import java.util.List;

@Tag(name = "Поддержка: Сообщения", description = "API для работы с сообщениями в чатах поддержки")
@SecurityRequirement(name = "JWT") // Требует JWT токен для всех методов
public interface ISupportMessageRestController {

    @Operation(
            summary = "Проверить возможность отправки сообщения",
            description = "Проверяет, можно ли отправить сообщение в указанный чат (не превышен ли лимит отправки сообщений)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Можно отправлять сообщения"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибки при выполнении, возможно чат не найден или недоступен"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Превышен лимит отправки сообщений",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(
                                    value = "You temporarily exhausted the limit of sending support messages"
                            )
                    )
            )
    })
     ResponseEntity<?> checkMessageSendingAbility(
            @Parameter(
                    description = "ID чата поддержки",
                    required = true,
                    example = "123"
            ) Long chatId);


    @Operation(
            summary = "Получить все сообщения чата",
            description = "Возвращает все сообщения указанного чата поддержки для текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список сообщений успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SupportMessageResponseDTO.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибки при выполнении"
            )
    })
    List<SupportMessageResponseDTO> getAllChatMessages(
            @Parameter(hidden = true) Jwt jwt,

            @Parameter(
                    description = "ID чата поддержки",
                    required = true,
                    example = "123"
            ) Long chatId) throws UserNotFoundException;
}
