package com.example.order_service.mapper;

import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.client.grpc.UserGrpcClient;
import com.example.common.dto.cart.rest.CartItemResponseDTO;
import com.example.common.dto.product.rest.ProductMinimalInfoResponseDto;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.dto.user.rest.UserMinimalInfoDto;
import com.example.common.mapper.ProductMapper;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.enumeration.OrderLivingStatus;
import com.example.order_service.enumeration.OrderSortingStatus;
import com.example.order_service.repository.OrderItemRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;



@Mapper(componentModel = "spring")
public abstract class OrderMapper {

    @Autowired
    protected OrderItemRepository orderItemRepository;

    @Autowired
    protected OrderItemMapper orderItemMapper;

    @Mapping(target = "totalPriceInt", source = "totalPrice")
    @Mapping(target = "totalPriceView", expression = "java(com.example.common.service.CommonProductService.formatPrice(order.getTotalPrice()))")
    @Mapping(target = "orderStatusView", expression = "java(mapOrderStatus(order.getOrderStatus()))")
    @Mapping(target = "deliveryPriceInt", source = "deliveryPrice")
    @Mapping(target = "deliveryPriceView",expression = "java(com.example.common.service.CommonProductService.formatPrice(order.getDeliveryPrice()))")
    public abstract OrderResponseDto toOrderDTOWithNoMappingItems(Order order);


    public List<OrderResponseDto> toOrderResponseDTOs(List<Order> orders){

        return orders.stream().map(o->{
            OrderResponseDto orderDto = toOrderDTOWithNoMappingItems(o);
            orderDto.setItemDTOs(orderItemMapper.mapOrderItems(o.getOrderItems()));
            return orderDto;
        }).toList();

    }

    protected String mapOrderStatus(OrderLivingStatus orderStatus){

        return switch (orderStatus){
            case IN_WAY -> "В пути";
            case CANCELLED -> "Отменён";
            case DELIVERED -> "Доставлен";
            case IN_ASSEMBLING -> "В сборке";
            case GIVING_TO_COURIER_SERVICE -> "Передаётся курьерской службе";
        };

    }


}
