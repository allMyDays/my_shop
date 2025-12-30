package com.example.cart_wishlist.mapper;


import com.example.cart_wishlist.service.WishListService;
import com.example.cart_wishlist.dto.WishListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishListMapper {

    private final WishListService wishService;

    private final WishItemMapper itemMapper;

    public WishListResponseDTO toWishListResponseDTO(Long userId) {
    WishListResponseDTO listResponseDTO = new WishListResponseDTO();
        listResponseDTO.setTotalQuantity(wishService.getListSize(userId));
        listResponseDTO.setItemsDTOList(itemMapper.mapItems(wishService.getItems(userId,0)));
        return listResponseDTO;

    }


}
