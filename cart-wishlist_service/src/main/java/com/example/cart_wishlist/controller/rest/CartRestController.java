package com.example.cart_wishlist.controller.rest;

import com.example.cart_wishlist.exception.TooManyItemsException;
import com.example.cart_wishlist.mapper.CartItemMapper;
import com.example.cart_wishlist.mapper.CartMapper;
import com.example.cart_wishlist.service.CartService;
import com.example.common.dto.cart.rest.CartResponseDTO;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.cart.rest.CartItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.common.service.CommonProductService.formatPrice;
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
    public ResponseEntity<?> updateItemQuantityByOne(     // если возвращает ноль - товар удален
                                                    @PathVariable Long productId,
                                                    @RequestParam boolean increase,
                                                    @RequestParam(required = false) Integer pricePerProduct,
                                                    @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
         int newQuantity = cartService.updateProductQuantity(getMyUserEntityId(jwt),productId,increase);

        Map<String, Object> result = new HashMap<>();
        result.put("newQuantity", newQuantity);
        if(pricePerProduct != null) {
         result.put("totalPriceView", formatPrice(pricePerProduct*newQuantity));
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);

    }

    @GetMapping("/items")
    public List<CartItemResponseDTO> getCartItems(@RequestParam int offset, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {  // возвращает по 40 товаров начиная с offset
        return cartItemMapper.mapCartItems(cartService.getCartItems(getMyUserEntityId(jwt), offset));
    }

    @GetMapping("/size")
    public Integer getCartSize(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return cartService.getCartSize(getMyUserEntityId(jwt));
    }



    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long productId, @RequestParam int quantity, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        try{
        cartService.addItemToCart(getMyUserEntityId(jwt),productId,quantity);
        }catch (TooManyItemsException e){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(e.getMessage());
        }

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


    @GetMapping("/brief-items")
    public List<ProductIdAndQuantityDto> getBriefItems(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return cartService.getBriefItems(getMyUserEntityId(jwt));

    }


}
