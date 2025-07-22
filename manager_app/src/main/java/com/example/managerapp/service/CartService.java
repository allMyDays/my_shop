package com.example.managerapp.service;

import com.example.managerapp.entity.Cart;
import com.example.managerapp.entity.CartItem;
import com.example.managerapp.entity.MyUser;
import com.example.managerapp.repository.CartItemRepository;
import com.example.managerapp.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartProductRepository;

    private final UserService userService;

    public Cart getUserCart(OAuth2AuthenticationToken authentication){

        MyUser user = userService.getMyUserFromPostgres(authentication);

        return cartRepository.findById(user.getId()).orElseGet(()->{
                    Cart cart = new Cart();
                    cart.setUserID(user.getId());
                    return cartRepository.save(cart);
              }
        );
    }
    public void addItemToCart(OAuth2AuthenticationToken authentication, Long productID, int quantity){

        Cart cart = getUserCart(authentication);
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

    public void removeItemFromCart(OAuth2AuthenticationToken authentication, Long productID){

        Cart cart = getUserCart(authentication);

        cart.getItems().removeIf(p -> p.getProductId().equals(productID));
        cartRepository.save(cart);
    }
















}
