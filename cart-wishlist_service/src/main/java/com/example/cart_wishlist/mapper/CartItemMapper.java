package com.example.cart_wishlist.mapper;

import com.example.cart_wishlist.entity.CartItem;
import com.example.common.dto.cart.CartItemResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "title", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "previewImageFileName", ignore = true)
    CartItemResponseDTO toCartItemDTO(CartItem cartItem);

    List<CartItemResponseDTO> toCartItemDTOList(List<CartItem> cartItemList);





}
