package com.example.cart_wishlist.mapper;


import com.example.cart_wishlist.entity.Cart;
import com.example.cart_wishlist.entity.CartItem;
import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.dto.cart.CartItemResponseDTO;
import com.example.common.dto.product.ProductResponseDTO;
import com.example.common.dto.cart.CartResponseDTO;
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
    public abstract CartResponseDTO toCartDTO(Cart cart);

    public List<CartItemResponseDTO> mapCartItems(List<CartItem> items){

        List<CartItemResponseDTO> dtoList = cartItemMapper.toCartItemDTOList(items);

        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .toList();

        List<ProductResponseDTO> productDTOs = productGrpcClient.getProductsByIdsFullList(productIds);

        Map<Long, ProductResponseDTO> productMap = productDTOs.stream()
                .collect(Collectors.toMap(ProductResponseDTO::getId, Function.identity()));

        for(CartItemResponseDTO dto : dtoList){

            ProductResponseDTO productRecord = productMap.get(dto.getProductId());
            if(productRecord != null){
                dto.setTitle(productRecord.getTitle());
                dto.setPrice(productRecord.getPrice());
                dto.setPreviewImageFileName(productRecord.getPreviewImageFileName());

            }
        }
        return dtoList;
    }

    public int calculateTotalQuantity(List<CartItem> items){
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

    }



}
