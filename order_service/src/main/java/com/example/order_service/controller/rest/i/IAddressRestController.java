package com.example.order_service.controller.rest.i;

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

@Tag(name = "Адреса доставки", description = "API для работы с адресами доставки")
public interface IAddressRestController {


        @Operation(
                summary = "Получить подсказки по адресу",
                description = "Возвращает список подсказок для автодополнения адреса с использованием сервиса DaData"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Список подсказок успешно получен",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(type = "array",
                                        example = "[\"г Москва, ул Тверская, д 1\", \"г Москва, ул Тверская, д 2\"]")
                        )
                )
        })
        List<String> suggest(
                @Parameter(
                        description = "Частичный ввод адреса для поиска подсказок",
                        required = true,
                        example = "Москва Тверская"
                ) String query);


        @Operation(
                summary = "Установить адрес доставки",
                description = "Сохраняет адрес доставки для текущего пользователя"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Адрес успешно сохранен"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                ),
                @ApiResponse(
                        responseCode = "422",
                        description = "Адрес некорректен или не может быть использован",
                        content = @Content(mediaType = "text/plain",
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject(
                                        value = "Указанный адрес не корректен или не существует"
                                )
                        )
                )
        })
        @SecurityRequirement(name = "JWT")
        ResponseEntity<?> setAddress(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Полный адрес доставки",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject(
                                        value = "г Москва, ул Тверская, д 1, кв 10"
                                )
                        )
                ) String address,
                @Parameter(hidden = true) Jwt jwt);
    }








