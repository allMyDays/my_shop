package com.example.cart_wishlist.controller.rest.i;

import com.example.common.dto.cart.rest.CartItemResponseDTO;
import com.example.common.dto.cart.rest.CartResponseDTO;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Корзина", description = "Управление корзиной аутентифицированого пользователя. " +
        "Пользоваться контроллером может только владелец корзины")
@SecurityRequirement(name = "JWT")
public interface ICartRestController {

    @Operation(
            summary = "Получить корзину пользователя",
            description = "Возвращает полную информацию о корзине пользователя (первые 40 товаров)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Корзина успешно получена",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CartResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован"
    )
    CartResponseDTO getCart(
            @Parameter(hidden = true) // Скрываю JWT из UI, но указываю требование безопасности
            Jwt jwt) throws UserNotFoundException;

    @Operation(
            summary = "Изменить количество товара",
            description = "Увеличивает или уменьшает количество товара в корзине на 1 единицу. "
                    + "Если новое количество равно 0 - товар удаляется из корзины."
    )

    @ApiResponse(
                    responseCode = "200",
                    description = "Количество успешно обновлено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = java.util.Map.class,
                                    example = "{\"newQuantity\": 5, \"totalPriceView\": \"50 000₽ руб.\"}"))
            )

    ResponseEntity<?> updateItemQuantityByOne(
            @Parameter(description = "ID товара", required = true, example = "123")
            @PathVariable Long productId,

            @Parameter(description = "Увеличить количество? true = +1, false = -1",
                    required = true, example = "true")
            @RequestParam boolean increase,

            @Parameter(description = "Цена за единицу товара (опционально, для расчета стоимости нового количества товаров)",
                    example = "100")
            @RequestParam(required = false) Integer pricePerProduct,

            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Получить элементы корзины с пагинацией",
            description = "Возвращает по 40 товаров начиная с указанной позиции"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список элементов корзины",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CartItemResponseDTO.class, type = "array"))
    )
    List<CartItemResponseDTO> getCartItems(
            @Parameter(description = "Смещение (offset) для пагинации, начиная с 0",
                    required = true, example = "0") int offset,

            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Получить полное количество товаров в корзине",
            description = "Возвращает полное общее количество позиций в корзине в виде числа"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Количество товаров в корзине",
            content = @Content(schema = @Schema(type = "integer", example = "15"))
    )
  Integer getCartSize( @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;


    @Operation(
            summary = "Добавить товар в корзину",
            description = "Добавляет указанное количество товара в корзину пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "429", description = "Слишком много товаров в корзине (превышен лимит)")
    })
    ResponseEntity<?> addToCart(
            @Parameter(description = "ID товара", required = true, example = "123") Long productId,
            @Parameter(description = "Количество для добавления", required = true, example = "2") int quantity,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Удалить товар из корзины",
            description = "Полностью удаляет товар из корзины пользователя"
    )
    @ApiResponse(responseCode = "200", description = "Товар успешно удален")
    ResponseEntity<?> removeFromCart(
            @Parameter(description = "ID товара для удаления", required = true, example = "123") Long productId,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;


    @Operation(
            summary = "Получить ID всех товаров в корзине",
            description = "Возвращает список идентификаторов всех товаров, находящихся в корзине юзера"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список ID товаров",
            content = @Content(schema = @Schema(type = "array",
                    example = "[123, 456, 789]"))
    )
    List<Long> getProductIDs(
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Проверить наличие товара в корзине",
            description = "Проверяет, находится ли указанный товар в корзине пользователя."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Результат проверки",
            content = @Content(schema = @Schema(type = "boolean", example = "true"))
    )
    boolean isProductInCart(
            @Parameter(description = "ID товара для проверки", required = true, example = "123") Long productId,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;

    @Operation(
            summary = "Получить краткую информацию о товарах в корзине",
            description = "Возвращает полный список товаров в корзине в компактном формате (ID и количество)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список товаров",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProductIdAndQuantityDto.class, type = "array"))
    )
    List<ProductIdAndQuantityDto> getBriefItems( @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



}
