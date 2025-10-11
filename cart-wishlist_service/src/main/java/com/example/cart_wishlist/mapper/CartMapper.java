package com.example.cart_wishlist.mapper;


import com.example.cart_wishlist.service.CartService;
import com.example.common.dto.cart.rest.CartResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartMapper {

    private final CartService cartService;

    private final CartItemMapper cartItemMapper;

    public CartResponseDTO toCartResponseDTO(Long userId) {
    CartResponseDTO cartResponseDTO = new CartResponseDTO();
        cartResponseDTO.setTotalQuantity(cartService.getCartSize(userId));
        cartResponseDTO.setItemsDTOList(cartItemMapper.mapCartItems(cartService.getCartItems(userId,0)));
        return cartResponseDTO;

    }


}
