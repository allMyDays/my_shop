package com.example.managerapp.controller;

import com.example.managerapp.dto.CartDTO;
import com.example.managerapp.mapper.CartMapper;
import com.example.managerapp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    @GetMapping
    public CartDTO getCart(OAuth2AuthenticationToken authenticationToken){
        return cartMapper.toCartDTO(cartService.getUserCart(authenticationToken));

    }



    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long productId, @RequestParam int quantity, OAuth2AuthenticationToken authenticationToken){

        cartService.addItemToCart(authenticationToken,productId,quantity);

        return ResponseEntity.ok().build();

    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, OAuth2AuthenticationToken authenticationToken){
        cartService.removeItemFromCart(authenticationToken,productId);
        return ResponseEntity.ok().build();

    }












}
