package com.example.cart_wishlist.controller.rest.i;

import com.example.cart_wishlist.mapper.WishItemMapper;
import com.example.cart_wishlist.mapper.WishListMapper;
import com.example.cart_wishlist.service.WishListService;
import com.example.common.dto.wish.rest.WishItemResponseDTO;
import com.example.common.dto.wish.rest.WishListResponseDTO;
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

import java.util.List;

@Tag(name = "Избранное", description = "API для управления списком избранных товаров")
@SecurityRequirement(name = "JWT")
public interface IWishListRestController {

    @Operation(
            summary = "Получить список избранного",
            description = "Возвращает полную информацию о списке избранного пользователя и первые 40 товаров)")
    @ApiResponse(
            responseCode = "200",
            description = "Список избранного успешно получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WishListResponseDTO.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован")
    WishListResponseDTO getWishList( @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;





    @Operation(
            summary = "Получить элементы избранного с пагинацией",
            description = "Возвращает по 40 товаров из списка избранного, начиная с указанной позиции"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список элементов избранного",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WishItemResponseDTO.class, type = "array"))
    )
    List<WishItemResponseDTO> getItems(
            @Parameter(description = "Смещение (offset) для пагинации, начиная с 0", required = true, example = "0") int offset,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;




    @Operation(
            summary = "Получить полное количество товаров в избранном",
            description = "Возвращает полное общее количество товаров в списке избранного пользователя"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Количество товаров в избранном",
            content = @Content(schema = @Schema(type = "integer", example = "8"))
    )
    Long getListSize(@Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Добавить товар в избранное",
            description = "Добавляет товар в список избранного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Метод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "429", description = "Слишком много товаров в избранном (превышен лимит)")
    })
    ResponseEntity<?> addToWishList(
            @Parameter(description = "ID товара для добавления в избранное", required = true, example = "456") Long productId,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Удалить товар из избранного",
            description = "Удаляет товар из списка избранного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Метод успешно выполнен"),
    })


    ResponseEntity<?> removeFromWishList(
            @Parameter(description = "ID товара для удаления из избранного", required = true, example = "456") Long productId,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;

    @Operation(
            summary = "Получить ID всех товаров в избранном",
            description = "Возвращает список идентификаторов всех товаров, находящихся в избранном"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список ID товаров в избранном",
            content = @Content(schema = @Schema(type = "array",
                    example = "[123, 456, 789]"))
    )

   List<Long> getProductIDs(@Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;



    @Operation(
            summary = "Проверить наличие товара в избранном",
            description = "Проверяет, находится ли указанный товар в списке избранного пользователя"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Результат проверки",
            content = @Content(schema = @Schema(type = "boolean", example = "true"))
    )
    boolean isProductInWishList(
            @Parameter(description = "ID товара для проверки", required = true, example = "456") Long productId,
            @Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;
}
