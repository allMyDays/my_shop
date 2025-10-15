package com.example.cart_wishlist.controller.rest;

import com.example.cart_wishlist.mapper.WishItemMapper;
import com.example.cart_wishlist.mapper.WishListMapper;
import com.example.cart_wishlist.service.WishListService;
import com.example.common.dto.wish.rest.WishListResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.wish.rest.WishItemResponseDTO;
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
@RequestMapping("/api/wish-list")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WishListRestController {

    private final WishListService wishService;
    private final WishListMapper listMapper;
    private final WishItemMapper itemMapper;


    @GetMapping
    public WishListResponseDTO getWishList(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {  // возвращает первые 40 товаров

        return listMapper.toWishListResponseDTO(getMyUserEntityId(jwt));

    }

    @GetMapping("/items")
    public List<WishItemResponseDTO> getItems(@RequestParam int offset, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException { // возвращает по 40 товаров начиная с offset

        return itemMapper.mapItems(wishService.getItems(getMyUserEntityId(jwt),offset));

    }


    @GetMapping("/size")
    public Long getListSize(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return wishService.getListSize(getMyUserEntityId(jwt));
    }


    @PostMapping("/add")
    public ResponseEntity<?> addToWishList(@RequestParam Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        if (getListSize(jwt)>=8000){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Вы превысили лимит на добавление товаров в список желаний. Удалите часть товаров, чтобы освободить место.");
        }

        wishService.addItem(getMyUserEntityId(jwt), productId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId:\\d+}")
    public ResponseEntity<?> removeFromWishList(@PathVariable Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        wishService.removeItem(getMyUserEntityId(jwt),productId);
        return ResponseEntity.ok().build();

    }

    @GetMapping("/get_product_ids")
    public List<Long> getProductIDs(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {  // возвращает id всех товаров
        return wishService.getProductIdsByUserId(getMyUserEntityId(jwt));
    }

    @GetMapping("/product-exists")
    public boolean isProductInWishList(@RequestParam Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return wishService.productExists(productId,getMyUserEntityId(jwt));
    }












}
