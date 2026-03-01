package com.example.cart_wishlist.mapper;


import com.example.cart_wishlist.entity.WishItem;
import com.example.cart_wishlist.service.WishListService;
import com.example.cart_wishlist.dto.WishListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishListMapper {

    private final WishItemMapper itemMapper;


    public WishListResponseDTO toWishListResponseDTO(long wishListSize, List<WishItem> wishItems) {
        WishListResponseDTO listResponseDTO = new WishListResponseDTO();
        listResponseDTO.setTotalQuantity(wishListSize);
        listResponseDTO.setItemsDTOList(itemMapper.mapItems(wishItems));
        return listResponseDTO;

    }


}
