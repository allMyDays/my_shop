package com.example.order_service.controller.rest;

import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.UserNotFoundException;
import com.example.order_service.controller.rest.i.iDeliveryRestController;
import com.example.order_service.dto.DeliveryInfoDto;
import com.example.order_service.dto.OrderItemResponseDto;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.entity.DeliveryInfo;
import com.example.order_service.enumeration.OrderSortingStatus;
import com.example.order_service.exception.AddressNotFoundException;
import com.example.order_service.exception.OrderAlreadyCancelledException;
import com.example.order_service.exception.OrderTooOldToCancelException;
import com.example.order_service.mapper.DeliveryInfoMapper;
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
import java.util.Optional;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequestMapping("/api/order/delivery")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeliveryRestController implements iDeliveryRestController {

    private final DeliveryInfoService deliveryInfoService;

    private final DeliveryInfoMapper deliveryInfoMapper;



    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public DeliveryInfoDto getDeliveryInfo(@AuthenticationPrincipal Jwt jwt){

        Long userId = getMyUserEntityId(jwt);
        Optional<DeliveryInfo> deliveryInfoOptional = deliveryInfoService.getDeliveryInfo(userId);

        if(deliveryInfoOptional.isEmpty()){
            throw new EntityNotFoundException(DeliveryInfo.class, userId);
        }

        return deliveryInfoMapper.toDeliveryInfoDto(deliveryInfoOptional.get());

    }

}
