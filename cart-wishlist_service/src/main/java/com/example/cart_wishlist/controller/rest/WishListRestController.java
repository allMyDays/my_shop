package com.example.cart_wishlist.controller.rest;

import com.example.cart_wishlist.mapper.WishListMapper;
import com.example.cart_wishlist.service.WishListService;
import com.example.common.dto.wish.WishListResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.wish.WishItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wish-list")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WishListRestController {

    private final WishListService wishListService;
    private final WishListMapper wishListMapper;


    @GetMapping
    public WishListResponseDTO getWishList(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        return wishListMapper.toWishListDTO(wishListService.getUserWishList(jwt));

    }

    @GetMapping("/items")
    public List<WishItemResponseDTO> getWishListItems(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        return getWishList(jwt).getItemsDTOList();

    }


    @GetMapping("/size")
    public Map<String, Integer> getListSize(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return Map.of("count",wishListService.getUserWishList(jwt).getProductIDs().size());
    }


    @PostMapping("/add")
    public ResponseEntity<?> addToWishList(@RequestParam Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        wishListService.addItemToWishList(jwt, productId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishList(@PathVariable Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        wishListService.removeItemFromWishList(jwt,productId);
        return ResponseEntity.ok().build();

    }












}
