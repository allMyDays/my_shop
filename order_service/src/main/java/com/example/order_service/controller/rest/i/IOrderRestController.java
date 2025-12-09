package com.example.order_service.controller.rest.i;

import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.UserNotFoundException;
import com.example.order_service.dto.OrderItemResponseDto;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.enumeration.OrderSortingStatus;
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

@Tag(name = "Заказы", description = "API для управления заказами пользователя")
@SecurityRequirement(name = "JWT")
public interface IOrderRestController {

        @Operation(
                summary = "Создать заказ",
                description = "Создает новый заказ на основе списка товаров, переданных в метод"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Заказ успешно создан"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Нельзя создать заказ (адрес доставки не указан)",
                        content = @Content(mediaType = "text/plain",
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject(value = "Для создания заказа необходимо указать адрес доставки")
                        )
                )
        })
        ResponseEntity<?> createOrder(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Список товаров для заказа",
                        required = true,
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ProductIdAndQuantityDto.class, type = "array"),
                                examples = @ExampleObject(value = """
                        [
                          {"productId": 123, "quantity": 2},
                          {"productId": 456, "quantity": 1}
                        ]
                        """)
                        )
                ) List<ProductIdAndQuantityDto> items,
                @Parameter(hidden = true) Jwt jwt);




        @Operation(
                summary = "Проверить возможность создания заказа",
                description = "Проверяет, может ли пользователь создать заказ (проверяется наличие адреса доставки)"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Заказ можно создать (адрес доставки указан)"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Нельзя создать заказ (адрес доставки не указан)"
                )
        })
        ResponseEntity<?> checkCreationOrderAbility(@Parameter(hidden = true) Jwt jwt);


        @Operation(
                summary = "Отменить заказ",
                description = "Отменяет существующий заказ с опцией возврата товаров в корзину"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Заказ успешно отменен"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Возможные ошибки (например, заказ не найден)"
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Нельзя отменить заказ",
                        content = @Content(mediaType = "text/plain",
                                schema = @Schema(type = "string"),
                                examples = {
                                        @ExampleObject(
                                                name = "Заказ уже отменен",
                                                value = "Заказ уже был отменен ранее"
                                        ),
                                        @ExampleObject(
                                                name = "Заказ слишком старый",
                                                value = "Невозможно отменить заказ, так как прошло слишком много времени"
                                        )
                                }
                        )
                )
        })
        ResponseEntity<?> cancelOrder(
                @Parameter(description = "ID заказа для отмены", required = true, example = "789") Long orderId,
                @Parameter(description = "Вернуть товары в корзину?", required = true, example = "true") boolean returnItemsToCart,
                @Parameter(hidden = true) Jwt jwt);


        @Operation(
                summary = "Получить список заказов",
                description = "Возвращает список заказов пользователя с пагинацией и фильтрацией по статусу." +
                        " Возвращает по 40 заказов начиная с offset, в каждом заказе первые 5 товаров"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Список заказов успешно получен",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = OrderResponseDto.class, type = "array"))
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                )
        })
        public List<OrderResponseDto> getOrders(
                @Parameter(
                        description = "Статус для сортировки заказов",
                        required = true,
                        schema = @Schema(implementation = OrderSortingStatus.class,
                                allowableValues = {"ACTIVE", "COMPLETED", "CANCELLED", "ALL"}),
                        example = "ACTIVE"
                ) OrderSortingStatus sortingStatus,
                @Parameter(
                        description = "Смещение для пагинации (возвращает по 40 заказов)",
                        required = true,
                        example = "0"
                ) int offset,
                @Parameter(hidden = true) Jwt jwt);


        @Operation(
                summary = "Получить товары заказа",
                description = "Возвращает список товаров конкретного заказа с пагинацией. Возвращает по 40 товаров заказа начиная с offset"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Список товаров заказа успешно получен",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = OrderItemResponseDto.class, type = "array"))
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Пользователь не аутентифицирован"
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "Заказ не принадлежит пользователю"
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "Заказ не найден"
                )
        })
        List<OrderItemResponseDto> getOrderItems(
                @Parameter(description = "ID заказа", required = true, example = "789") Long orderId,
                @Parameter(description = "Смещение для пагинации (возвращает по 40 товаров)",
                        required = true, example = "0") int offset,
                @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;
    }







