package com.example.managerapp.mapper;

import com.example.managerapp.dto.cart.CartItemDTO;
import com.example.managerapp.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "title", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "previewImageFileName", ignore = true)
    CartItemDTO toCartItemDTO(CartItem cartItem);

    List<CartItemDTO> toCartItemDTOList(List<CartItem> cartItemList);





}
