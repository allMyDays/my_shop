package com.example.cart_wishlist.controller.rest;

import com.example.cart_wishlist.mapper.CartMapper;
import com.example.cart_wishlist.service.CartService;
import com.example.common.dto.cart.CartResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.cart.CartItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public CartResponseDTO getCart(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return cartMapper.toCartDTO(cartService.getUserCart(jwt));

    }
    @GetMapping("/items")
    public List<CartItemResponseDTO> getCartItems(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return getCart(jwt).getItemsDTOList();
    }

    @GetMapping("/size")
    public Map<String, Integer> getCartSize(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return Map.of("count",cartMapper.calculateTotalQuantity(cartService.getUserCart(jwt).getItems()));
    }



    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long productId, @RequestParam int quantity, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        cartService.addItemToCart(jwt,productId,quantity);

        return ResponseEntity.ok().build();

    }

    @DeleteMapping("/{productId:\\d+}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        cartService.removeItemFromCart(jwt,productId);
        return ResponseEntity.ok().build();

    }












}
