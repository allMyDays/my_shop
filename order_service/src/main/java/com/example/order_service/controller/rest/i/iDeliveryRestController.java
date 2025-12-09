package com.example.order_service.controller.rest.i;

import com.example.order_service.dto.DeliveryInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Информация о доставке", description = "API для работы с инормацией о доставке заказов")
public interface iDeliveryRestController {


    @Operation(
            summary = "Получить информацию о доставке",
            description = "Отдает информацию о доставке любого заказа к адресу пользователя (дистанцию, время, цену доставки и т.д.)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация успешно получена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            )
    })
    DeliveryInfoDto getDeliveryInfo(Jwt jwt);







}
