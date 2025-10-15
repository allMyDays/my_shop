package com.example.cart_wishlist.controller.rest;

import com.example.cart_wishlist.mapper.CartItemMapper;
import com.example.cart_wishlist.mapper.CartMapper;
import com.example.cart_wishlist.service.CartService;
import com.example.common.dto.cart.rest.CartResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.cart.rest.CartItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartRestController {

    private final CartService cartService;
    private final CartItemMapper cartItemMapper;
    private final CartMapper cartMapper;

    @GetMapping
    public CartResponseDTO getCart(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException{  // возвращает первые 40 товаров
        return cartMapper.toCartResponseDTO(getMyUserEntityId(jwt));
    }

    @PutMapping("/{productId:\\d+}")
    public Integer updateItemQuantityByOne(@PathVariable Long productId, @RequestParam boolean increase, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException { // если возвращает ноль - товар удален
        return cartService.updateProductQuantity(getMyUserEntityId(jwt),productId,increase);
    }

    @GetMapping("/items")
    public List<CartItemResponseDTO> getCartItems(@RequestParam int offset, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {  // возвращает по 40 товаров начиная с offset
        return cartItemMapper.mapCartItems(cartService.getCartItems(getMyUserEntityId(jwt), offset));
    }

    @GetMapping("/size")
    public Long getCartSize(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return cartService.getCartSize(getMyUserEntityId(jwt));
    }



    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long productId, @RequestParam int quantity, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        if(getCartSize(jwt)>=4000){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Вы превысили лимит на добавление товаров в корзину. Совершите заказ товаров или удалите их, чтобы освободить место.");
        }

        cartService.addItemToCart(getMyUserEntityId(jwt),productId,quantity);

        return ResponseEntity.ok().build();

    }

    @DeleteMapping("/{productId:\\d+}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        cartService.removeItemFromCart(getMyUserEntityId(jwt),productId);
        return ResponseEntity.ok().build();

    }

    @GetMapping("/get_product_ids")
    public List<Long> getProductIDs(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {  // возвращает id всех товаров
        return cartService.getProductIdsByUserId(getMyUserEntityId(jwt));
    }

    @GetMapping("/product-exists")
    public boolean isProductInCart(@RequestParam Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return cartService.productExists(productId,getMyUserEntityId(jwt));
    }












}
