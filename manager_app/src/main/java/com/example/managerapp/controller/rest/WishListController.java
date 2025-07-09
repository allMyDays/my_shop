package com.example.managerapp.controller.rest;

import com.example.managerapp.dto.WishItemDTO;
import com.example.managerapp.dto.WishListDTO;
import com.example.managerapp.mapper.WishListMapper;
import com.example.managerapp.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/items")
    public List<WishItemDTO> getWishListItems(OAuth2AuthenticationToken authentication) {

        return getWishList(authentication).getItemsDTOList();

    }


    @GetMapping("/size")
    public Map<String, Integer> getListSize(OAuth2AuthenticationToken authenticationToken){
        return Map.of("count",wishListService.getUserWishList(authenticationToken).getProductIDs().size());
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
