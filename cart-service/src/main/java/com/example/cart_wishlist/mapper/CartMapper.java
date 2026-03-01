package com.example.cart_wishlist.mapper;


import com.example.cart_wishlist.entity.CartItem;
import com.example.cart_wishlist.service.CartService;
import com.example.common.dto.cart.rest.CartResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartMapper {

    private final CartItemMapper cartItemMapper;


    public CartResponseDTO toCartResponseDTO(long cartSize, List<CartItem> cartItems ) {
        CartResponseDTO cartResponseDTO = new CartResponseDTO();
        cartResponseDTO.setTotalQuantity(cartSize);
        cartResponseDTO.setItemsDTOList(cartItemMapper.mapCartItems(cartItems));
        return cartResponseDTO;

    }

}
