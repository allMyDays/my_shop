package com.example.managerapp.controller;

import com.example.managerapp.dto.WishListDTO;
import com.example.managerapp.entity.WishList;
import com.example.managerapp.mapper.WishListMapper;
import com.example.managerapp.service.UserService;
import com.example.managerapp.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wish-list")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;
    private final WishListMapper wishListMapper;


    @GetMapping
    public WishListDTO getWishList(OAuth2AuthenticationToken authentication) {

        return wishListMapper.toWishListDTO(wishListService.getUserWishList(authentication));

    }


    @PostMapping("/add")
    public ResponseEntity<?> addToWishList(@RequestParam Long productId, OAuth2AuthenticationToken authenticationToken){

        wishListService.addItemToWishList(authenticationToken, productId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, OAuth2AuthenticationToken authenticationToken){
        wishListService.removeItemFromWishList(authenticationToken,productId);
        return ResponseEntity.ok().build();

    }












}
