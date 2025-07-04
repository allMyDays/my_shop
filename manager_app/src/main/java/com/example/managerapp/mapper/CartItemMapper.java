package com.example.managerapp.mapper;

import com.example.managerapp.dto.CartItemDTO;
import com.example.managerapp.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "title", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "previewImageID", ignore = true)
    CartItemDTO toCartItemDTO(CartItem cartItem);

    List<CartItemDTO> toCartItemDTOList(List<CartItem> cartItemList);





}
