package com.example.cart_wishlist.service;

import com.example.cart_wishlist.entity.Cart;
import com.example.cart_wishlist.entity.CartItem;
import com.example.cart_wishlist.exception.TooManyItemsException;
import com.example.cart_wishlist.repository.CartItemRepository;
import com.example.cart_wishlist.repository.CartRepository;
import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;


    private final ProductGrpcClient productGrpcClient;
    private final CartItemRepository cartItemRepository;

    public Cart getOrCreateUserCart(long userId){

        return cartRepository.findById(userId).orElseGet(()->{
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    return cartRepository.save(cart);
                }
        );
    }

    public void addItemToCart(long userId, long productID, int quantity) {

        if(getCartSize(userId)>=4000){
           throw new TooManyItemsException(true);
        }
        if (!productGrpcClient.productExists(productID)){
            throw new ProductNotFoundException(List.of(productID));
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

    public int addItemsToCart(long userId, List<ProductIdAndQuantityDto> productDTOs) {

        Cart cart = getOrCreateUserCart(userId);

        int availableSize = 4000-getCartSize(userId);

        if (availableSize<=0) return 0;

        Map<Long, Integer> productMap = productDTOs.stream()
                .collect(Collectors.toMap(ProductIdAndQuantityDto::getProductId, ProductIdAndQuantityDto::getProductQuantity, Integer::sum));

        List<Long> existingProducts = productGrpcClient.productsExist(productMap.keySet().stream().toList());

        int addedQuantity=0;
        for(Long productId: existingProducts){
            if(availableSize<=0) return addedQuantity;
            int requiredQuantity = productMap.get(productId);

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductId(productId);

            if(availableSize<=requiredQuantity){
                cartItem.setQuantity(availableSize);
                cartItemRepository.save(cartItem);
                return addedQuantity+availableSize;
            }

            cartItem.setQuantity(requiredQuantity);
            cartItemRepository.save(cartItem);
            addedQuantity+=requiredQuantity;
            availableSize-=requiredQuantity;
        }
        return addedQuantity;
    }

    public void removeItemFromCart(long userId, long productID) {

        getOrCreateUserCart(userId);

        cartItemRepository.deleteByUserIdAndProductId(userId,productID);
    }
    public void removeItemsFromCart(long userId, List<Long> productIDs) {

        getOrCreateUserCart(userId);

        cartItemRepository.deleteByUserIdAndProductIdIn(userId,productIDs);
    }


    public List<CartItem> getCartItems(long userId, int offset){

        if(offset<0) throw new IllegalArgumentException("offset must be greater or equal to 0");

        getOrCreateUserCart(userId);

        int limit = 40;

        Pageable pageable = PageRequest.of(offset/limit, limit);

        return cartItemRepository.findByUserId(userId, pageable);

    }

    public int getCartSize(long userId){

        getOrCreateUserCart(userId);

        return cartItemRepository.sumQuantityByUserId(userId);
    }

    public int updateProductQuantity(long userId, long productId, boolean increase){

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

    public List<Long> getProductIdsByUserId(long userId){
        return cartItemRepository.findProductIdsByUserId(userId);
    }

    public boolean productExists(long productId, long userId){
        return cartItemRepository.existsByCartUserIdAndProductId(userId,productId);
    }

    public List<ProductIdAndQuantityDto> getBriefItems(long userId){

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        return cartItems.stream().map(a->new ProductIdAndQuantityDto(a.getProductId(),a.getQuantity()))
                .collect(Collectors
                        .toList());


    }

}
