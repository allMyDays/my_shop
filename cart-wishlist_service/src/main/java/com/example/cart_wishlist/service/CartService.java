package com.example.cart_wishlist.service;

import com.example.cart_wishlist.entity.Cart;
import com.example.cart_wishlist.entity.CartItem;
import com.example.cart_wishlist.repository.CartItemRepository;
import com.example.cart_wishlist.repository.CartRepository;
import com.example.common.client.grpc.ProductGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;


    private final ProductGrpcClient productGrpcClient;
    private final CartItemRepository cartItemRepository;

    public Cart getOrCreateUserCart(Long userId){

        return cartRepository.findById(userId).orElseGet(()->{
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    return cartRepository.save(cart);
                }
        );
    }

    public void addItemToCart(Long userId, Long productID, int quantity) {

        if (!productGrpcClient.productExists(productID)){
            return;
        }
        Cart cart = getOrCreateUserCart(userId);

        Optional<CartItem> existingProduct = cartItemRepository.findByCartUserIdAndProductId(userId,productID);

        CartItem cartItem;
        if(existingProduct.isPresent()){
            cartItem = existingProduct.get();
            cartItem.setQuantity(cartItem.getQuantity()+quantity);
            cartRepository.save(cart);

        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductId(productID);
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
            cartRepository.save(cart);
        }

    }

    public void removeItemFromCart(Long userId, Long productID) {

        if (!productGrpcClient.productExists(productID)){
            return;
        }
        getOrCreateUserCart(userId);

        cartItemRepository.deleteByUserIdAndProductId(userId,productID);
    }


    public List<CartItem> getCartItems(Long userId, int offset){

        if(offset<0) throw new IllegalArgumentException("offset must be greater than 0");

        getOrCreateUserCart(userId);

        int limit = 40;

        Pageable pageable = PageRequest.of(offset/limit, limit);

        return cartItemRepository.findByUserId(userId, pageable);

    }

    public long getCartSize(Long userId){

        getOrCreateUserCart(userId);

        return cartItemRepository.sumQuantityByUserId(userId);
    }

    public int updateProductQuantity(Long userId, Long productId, boolean increase){

        Optional<CartItem> cartItemOptional = cartItemRepository.findByCartUserIdAndProductId(userId,productId);
        if (cartItemOptional.isEmpty()) return 0;
        CartItem cartItem = cartItemOptional.get();

        int quantity = cartItem.getQuantity();
        quantity+=increase?1:-1;
        if(quantity<=0){
            cartItemRepository.deleteByUserIdAndProductId(userId,productId);
            return 0;
        }
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return quantity;

    }

    public List<Long> getProductIdsByUserId(Long userId){
        return cartItemRepository.findProductIdsByUserId(userId);
    }




}
