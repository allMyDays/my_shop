package com.example.managerapp.service;

import com.example.managerapp.entity.*;
import com.example.managerapp.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;

    private final UserService userService;



    public WishList getUserWishList(OAuth2AuthenticationToken authentication){

        MyUser user = userService.getMyUserFromBD(userService.getUserID(authentication));

        return wishListRepository.findById(user.getId()).orElseGet(()->{
                    WishList wishList = new WishList();
                    wishList.setUserID(user.getId());
                    return wishListRepository.save(wishList);
                 }

        );
    }

    public void addItemToWishList(OAuth2AuthenticationToken authentication, Long productID){

        WishList wishList = getUserWishList(authentication);

        if(wishList.getProductIDs().contains(productID)) return;

        wishList.getProductIDs().add(productID);

        wishListRepository.save(wishList);



    }


    public void removeItemFromWishList(OAuth2AuthenticationToken authentication, Long productID){

        WishList wishList = getUserWishList(authentication);

        wishList.getProductIDs().remove(productID);

        wishListRepository.save(wishList);
    }








}
