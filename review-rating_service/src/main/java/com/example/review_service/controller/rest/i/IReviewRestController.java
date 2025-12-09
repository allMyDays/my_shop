package com.example.review_service.controller.rest.i;


import com.example.common.exception.UserNotFoundException;
import com.example.review_service.dto.CreateReviewRequestDto;
import com.example.review_service.dto.EditReviewRequestDto;
import com.example.review_service.dto.ProductReviewInfoDto;
import com.example.review_service.dto.ReviewResponseDto;
import com.example.review_service.enumeration.EditReviewAbilityStatus;
import com.example.review_service.enumeration.ReviewSortType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Отзывы", description = "API для работы с отзывами на товары")
public interface IReviewRestController {


    @Operation(
            summary = "Создать отзыв",
            description = "Создает новый отзыв на товар с возможностью загрузки изображений"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв успешно создан"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибки валидации данных",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array",
                                    example = "[\"Комментарий отзыва должен иметь длину от 5 до 500 символов.\"]")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Отзыв данного пользователя на данный товар уже существует",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Вы уже оставили отзыв на этот товар")
                    )
            )
    })
    @SecurityRequirement(name = "JWT")
    ResponseEntity<?> createReview(
            @Parameter(
                    description = "Данные отзыва",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(implementation = CreateReviewRequestDto.class))
            ) CreateReviewRequestDto productReviewDto,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(
                    description = "Изображения к отзыву",
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary"))
            ) List<MultipartFile> images,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Редактировать отзыв",
            description = "Редактирует существующий отзыв с возможностью обновления изображений"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв успешно отредактирован"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибки валидации данных или иные ошибки(Отзыв не принадлежит пользователю, отзыв не найден)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Нет изменений для сохранения",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Не обнаружено изменений для сохранения")
                    )
            )
    })
    @SecurityRequirement(name = "JWT")
    ResponseEntity<?> editReview(
            @Parameter(
                    description = "Обновленные данные отзыва",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(implementation = EditReviewRequestDto.class))
            ) EditReviewRequestDto reviewDto,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(
                    description = "Новые изображения для отзыва",
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary"))
            ) List<MultipartFile> images,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Проверить возможность редактирования отзыва",
            description = "Проверяет, может ли пользователь редактировать указанный отзыв"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Результат проверки",
                    content = @Content(schema = @Schema(implementation = EditReviewAbilityStatus.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            )
    })
    @SecurityRequirement(name = "JWT")
    EditReviewAbilityStatus checkEditingReviewAbility(
            @Parameter(description = "ID отзыва", required = true, example = "123") Long reviewId,
            @Parameter(hidden = true) Jwt jwt);



    @Operation(
            summary = "Удалить отзыв по ID",
            description = "Удаляет отзыв по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв успешно удален"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ошибки(Отзыв не принадлежит пользователю, отзыв не найден)"
            )
    })
    @SecurityRequirement(name = "JWT")
    ResponseEntity<?> removeReviewByReviewId(
            @Parameter(description = "ID отзыва (только цифры)", required = true, example = "123") Long reviewId,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Удалить отзыв по ID товара",
            description = "Удаляет отзыв пользователя на указанный товар"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв успешно удален"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ошибки(Отзыв не принадлежит пользователю, отзыв не найден)"
            )
    })
    @SecurityRequirement(name = "JWT")
    ResponseEntity<?> removeReviewByProductId(
            @Parameter(description = "ID товара (только цифры)", required = true, example = "456") Long productId,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Получить отзывы на товар",
            description = "Возвращает отзывы на товар с пагинацией и сортировкой"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список отзывов успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponseDto.class, type = "array"))
            )
    })
    List<ReviewResponseDto> getReviews(
            @Parameter(description = "ID товара (только цифры)", required = true, example = "789") long productId,
            @Parameter(description = "Тип сортировки", required = true,
                    schema = @Schema(implementation = ReviewSortType.class,
                            allowableValues = {"HIGH_RATING", "LOW_RATING", "NEWEST", "WITH_PHOTO", "MY_REVIEW"}),
                    example = "HIGH_RATING") ReviewSortType sortType,

            @Parameter(description = "Смещение для пагинации (возвращает по 40 отзывов)",
                    required = true, example = "0") int offset,

            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Получить информацию (количество отзывов, средняя оыценка) для списка товаров",
            description = "Возвращает агрегированную информацию о количестве отзывах и средную оценку для нескольких товаров"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация успешно получена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductReviewInfoDto.class, type = "array"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный список ID товаров"
            )
    })
    List<ProductReviewInfoDto> getProductsInfo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Список ID товаров",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", example = "[123, 456, 789]"))
            ) List<Long> productIds);



    @Operation(
            summary = "Получить информацию (количество отзывов, средняя оыценка) для одного товара",
            description = "Возвращает агрегированную информацию о количестве отзывах и средную оценку для конкретного товара"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация успешно получена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductReviewInfoDto.class))
            )
    })
    ProductReviewInfoDto getProductInfo(
            @Parameter(description = "ID товара", required = true, example = "123") Long productId);



    @Operation(
            summary = "Получить минимальные данные отзыва",
            description = "Возвращает основные данные отзыва без информации о пользователе (без имени пользователя и аватарки)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отзыв успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Отзыв не найден",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Не удалось получить отзыв с id 123")
                    )
            )
    })
    ResponseEntity<?> getReview(
            @Parameter(description = "ID отзыва (только цифры)", required = true, example = "456") Long reviewId);
}