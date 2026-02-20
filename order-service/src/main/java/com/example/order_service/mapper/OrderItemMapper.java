package com.example.order_service.mapper;

import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.dto.cart.rest.CartItemResponseDTO;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.dto.product.rest.ProductMinimalInfoResponseDto;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.mapper.ProductMapper;
import com.example.order_service.dto.OrderItemResponseDto;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.repository.OrderItemRepository;
import lombok.NonNull;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.common.service.CommonProductService.formatPrice;

@Mapper(componentModel = "spring")
public abstract class OrderItemMapper {

    @Autowired
    protected ProductGrpcClient productGrpcClient;

    @Autowired
    protected ProductMapper productMapper;

    public List<OrderItemResponseDto> mapOrderItems(@NonNull List<OrderItem> orderItems) {

        List<ProductResponseDTO> productDTOs = productGrpcClient.getProductsByIdsFullList(
                orderItems.stream()
                        .map(OrderItem::getProductId)
                        .collect(Collectors.toList())
        );

        Map<Long, ProductResponseDTO> productMap = productDTOs.stream()
                .collect(Collectors.toMap(ProductResponseDTO::getId, Function.identity()));

        List<OrderItemResponseDto> resultList = new ArrayList<>();

        for(OrderItem item : orderItems){
            OrderItemResponseDto orderDto = new OrderItemResponseDto();
            orderDto.setProductId(item.getProductId());
            orderDto.setProductQuantity(item.getProductQuantity());
            orderDto.setPriceInt(item.getProductPrice());
            orderDto.setPriceView(formatPrice(item.getProductPrice()));

            ProductResponseDTO productDto = productMap.get(item.getProductId());
            if(productDto != null){
                orderDto.setTitle(productDto.getTitle());
                orderDto.setPreviewImageFileName(productDto.getPreviewImageFileName());

            } resultList.add(orderDto);
        }
        return resultList;


    }

    public abstract List<ProductIdAndQuantityDto> toProductIdAndQuantityDTOs(List<OrderItem> orderItems);


}
