package com.example.managerapp.controller.rest;

import com.example.managerapp.dto.CartDTO;
import com.example.managerapp.dto.CartItemDTO;
import com.example.managerapp.mapper.CartMapper;
import com.example.managerapp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartRestController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    @GetMapping
    public CartDTO getCart(OAuth2AuthenticationToken authenticationToken){
        return cartMapper.toCartDTO(cartService.getUserCart(authenticationToken));

    }
    @GetMapping("/items")
    public List<CartItemDTO> getCartItems(OAuth2AuthenticationToken authenticationToken){
        return getCart(authenticationToken).getItemsDTOList();
    }

    @GetMapping("/size")
    public Map<String, Integer> getCartSize(OAuth2AuthenticationToken authenticationToken){
        return Map.of("count",cartService.getUserCart(authenticationToken).getItems().size());
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
