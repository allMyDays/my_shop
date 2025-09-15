package com.example.cart_wishlist.service;

import com.example.cart_wishlist.entity.WishList;
import com.example.cart_wishlist.repository.WishListRepository;
import com.example.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;




    public WishList getUserWishList(Jwt jwt) throws UserNotFoundException {

        Long userId = getMyUserEntityId(jwt);

        return wishListRepository.findById(userId).orElseGet(()->{
                    WishList wishList = new WishList();
                    wishList.setUserID(userId);
                    return wishListRepository.save(wishList);
                 }

        );
    }

    public void addItemToWishList(Jwt jwt, Long productID) throws UserNotFoundException {

        WishList wishList = getUserWishList(jwt);

        if(wishList.getProductIDs().contains(productID)) return;

        wishList.getProductIDs().add(productID);

        wishListRepository.save(wishList);

    }


    public void removeItemFromWishList(Jwt jwt, Long productID) throws UserNotFoundException {

        WishList wishList = getUserWishList(jwt);

        wishList.getProductIDs().remove(productID);

        wishListRepository.save(wishList);
    }








}
