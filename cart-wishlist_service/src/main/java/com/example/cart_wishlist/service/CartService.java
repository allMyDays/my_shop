package com.example.cart_wishlist.service;

import com.example.cart_wishlist.entity.Cart;
import com.example.cart_wishlist.entity.CartItem;
import com.example.cart_wishlist.repository.CartItemRepository;
import com.example.cart_wishlist.repository.CartRepository;
import com.example.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartProductRepository;

    public Cart getUserCart(Jwt jwt) throws UserNotFoundException {

        Long userId = getMyUserEntityId(jwt);

        return cartRepository.findById(userId).orElseGet(()->{
                    Cart cart = new Cart();
                    cart.setUserID(userId);
                    return cartRepository.save(cart);
              }
        );
    }
    public void addItemToCart(Jwt jwt, Long productID, int quantity) throws UserNotFoundException {

        Cart cart = getUserCart(jwt);
        Optional<CartItem> existingProduct = cart.getItems().stream()
                .filter(p -> p.getProductId().equals(productID))
                .findFirst();

        if(existingProduct.isPresent()){

            existingProduct.get().setQuantity(existingProduct.get().getQuantity()+quantity);

        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductId(productID);
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
        }
        cartRepository.save(cart);
    }

    public void removeItemFromCart(Jwt jwt, Long productID) throws UserNotFoundException {

        Cart cart = getUserCart(jwt);

        cart.getItems().removeIf(p -> p.getProductId().equals(productID));
        cartRepository.save(cart);
    }
















}
