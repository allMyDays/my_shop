package com.example.managerapp.mapper;


import com.example.managerapp.client.grpc.ProductGrpcClient;
import com.example.managerapp.dto.cart.CartItemDTO;
import com.example.managerapp.dto.cart.CartDTO;
import com.example.managerapp.dto.product.ProductResponseDTO;
import com.example.managerapp.entity.Cart;
import com.example.managerapp.entity.CartItem;
import com.example.managerapp.client.rest.ProductRestClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class CartMapper {
    
    @Autowired
    protected ProductGrpcClient productGrpcClient;

    @Autowired
    protected CartItemMapper cartItemMapper;
    
    
    
    
    @Mapping(target = "itemsDTOList", expression = "java(mapCartItems(cart.getItems()))")
    @Mapping(target = "totalQuantity", expression = "java(calculateTotalQuantity(cart.getItems()))")
    public abstract CartDTO toCartDTO(Cart cart);

    protected List<CartItemDTO> mapCartItems(List<CartItem> items){
        
        List<CartItemDTO> dtoList = cartItemMapper.toCartItemDTOList(items);

        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .toList();

        List<ProductResponseDTO> productRecords = productGrpcClient.getProductsByIds(productIds);

        Map<Long, ProductResponseDTO> productMap = productRecords.stream()
                .collect(Collectors.toMap(ProductResponseDTO::getId, Function.identity()));

        for(CartItemDTO dto : dtoList){

            ProductResponseDTO productRecord = productMap.get(dto.getProductId());
            if(productRecord != null){
                dto.setTitle(productRecord.getTitle());
                dto.setPrice(productRecord.getPrice());
                dto.setPreviewImageFileName(productRecord.getPreviewImageFileName());

            }
        }
        return dtoList;
    }

    protected int calculateTotalQuantity(List<CartItem> items){
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

    }



}
