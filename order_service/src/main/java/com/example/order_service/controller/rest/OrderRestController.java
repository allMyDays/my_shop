package com.example.order_service.controller.rest;
import com.example.common.client.grpc.UserGrpcClient;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.dto.product.rest.ProductMinimalInfoResponseDto;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.order_service.controller.rest.i.IOrderRestController;
import com.example.order_service.dto.DeliveryInfoDto;
import com.example.order_service.dto.OrderItemResponseDto;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.enumeration.OrderSortingStatus;
import com.example.order_service.exception.AddressNotFoundException;
import com.example.order_service.exception.OrderAlreadyCancelledException;
import com.example.order_service.exception.OrderTooOldToCancelException;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.service.DeliveryInfoService;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderRestController implements IOrderRestController {

    private final OrderService orderService;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DeliveryInfoService deliveryInfoService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody List<ProductIdAndQuantityDto> items, @AuthenticationPrincipal Jwt jwt) {

        try{
            orderService.createOrder(getMyUserEntityId(jwt), items);

        }catch (AddressNotFoundException e){
            return ResponseEntity.status(409)
                    .body(e.getMessage());
        }

        return ResponseEntity.ok().build();

    }
    @GetMapping("/create-ability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkCreationOrderAbility(@AuthenticationPrincipal Jwt jwt){
        Long userId = getMyUserEntityId(jwt);
        if(deliveryInfoService.getDeliveryInfo(userId).isEmpty()){
            return ResponseEntity.status(409)
                    .body(new AddressNotFoundException(userId).getMessage());

        } return ResponseEntity.ok()
                .build();


    }

    @PutMapping("/cancel")
    public ResponseEntity<?> cancelOrder(@RequestParam Long orderId, @RequestParam boolean returnItemsToCart, @AuthenticationPrincipal Jwt jwt) {

        try{
            orderService.cancelOrder(getMyUserEntityId(jwt), orderId, returnItemsToCart);
        }catch (OrderAlreadyCancelledException|OrderTooOldToCancelException e){
            return ResponseEntity.status(409)
                    .body(e.getMessage());
        }

        return ResponseEntity.ok().build();

    }


    // возвращает по 40 заказов начиная с offset, в каждом заказе первые 5 товаров
    @GetMapping
    public List<OrderResponseDto> getOrders(@RequestParam(defaultValue = "ACTIVE") OrderSortingStatus sortingStatus, @RequestParam int offset,  @AuthenticationPrincipal Jwt jwt) {

        return orderMapper.toOrderResponseDTOs(orderService.getOrders(sortingStatus,getMyUserEntityId(jwt),offset));

    }

    // возвращает по 40 товаров заказа начиная с offset
    @GetMapping("/items")
    public List<OrderItemResponseDto> getOrderItems(@RequestParam Long orderId, @RequestParam int offset, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        return orderItemMapper.mapOrderItems(orderService.getOrderItems(orderId,getMyUserEntityId(jwt),offset));

    }

}
