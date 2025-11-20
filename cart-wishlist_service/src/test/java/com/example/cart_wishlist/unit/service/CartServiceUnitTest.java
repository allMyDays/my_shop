package com.example.cart_wishlist.unit.service;

import com.example.cart_wishlist.entity.Cart;
import com.example.cart_wishlist.entity.CartItem;
import com.example.cart_wishlist.exception.TooManyItemsException;
import com.example.cart_wishlist.repository.CartItemRepository;
import com.example.cart_wishlist.repository.CartRepository;
import com.example.cart_wishlist.service.CartService;
import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductGrpcClient productGrpcClient;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    private final Long USER_ID = 1L;
    private final Long PRODUCT_ID = 100L;
    private final int QUANTITY = 2;

    @Test
    void getOrCreateUserCart_WhenCartExists_ReturnsExistingCart() {
        Cart expectedCart = new Cart();
        expectedCart.setUserId(USER_ID);
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(expectedCart));

        // When
        Cart result = cartService.getOrCreateUserCart(USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        verify(cartRepository).findById(USER_ID);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getOrCreateUserCart_WhenCartNotExists_CreatesNewCart() {
        // Given
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Cart newCart = new Cart();
        newCart.setUserId(USER_ID);
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        // When
        Cart result = cartService.getOrCreateUserCart(USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        verify(cartRepository).findById(USER_ID);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemToCart_WhenCartIsFull_ThrowsTooManyItemsException() {
        // Given
        when(cartItemRepository.sumQuantityByUserId(USER_ID)).thenReturn(4000);

        // When & Then
        assertThrows(TooManyItemsException.class, () ->
                cartService.addItemToCart(USER_ID, PRODUCT_ID, QUANTITY));

        verify(productGrpcClient, never()).productExists(anyLong());
        verify(cartItemRepository, never()).findByCartUserIdAndProductId(anyLong(), anyLong());
    }

    @Test
    void addItemToCart_WhenProductNotExists_ThrowsProductNotFoundException() {
        // Given
        when(cartItemRepository.sumQuantityByUserId(USER_ID)).thenReturn(10);
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(false);

        // When & Then
        assertThrows(ProductNotFoundException.class, () ->
                cartService.addItemToCart(USER_ID, PRODUCT_ID, QUANTITY));

        verify(productGrpcClient).productExists(PRODUCT_ID);
        verify(cartItemRepository, never()).findByCartUserIdAndProductId(anyLong(), anyLong());
    }

    @Test
    void addItemToCart_WhenProductExistsAndItemNotInCart_AddsNewItem() {
        // Given
        when(cartItemRepository.sumQuantityByUserId(USER_ID)).thenReturn(10);
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(true);

        Cart cart = new Cart();
        cart.setUserId(USER_ID);
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        cartService.addItemToCart(USER_ID, PRODUCT_ID, QUANTITY);

        // Then
        verify(cartRepository).save(cart);
        verify(cartItemRepository).findByCartUserIdAndProductId(USER_ID, PRODUCT_ID);
    }

    @Test
    void addItemToCart_WhenProductExistsAndItemInCart_UpdatesQuantity() {
        // Given
        when(cartItemRepository.sumQuantityByUserId(USER_ID)).thenReturn(10);
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(true);

        Cart cart = new Cart();
        cart.setUserId(USER_ID);
        CartItem existingItem = new CartItem();
        existingItem.setQuantity(3);

        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(existingItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        cartService.addItemToCart(USER_ID, PRODUCT_ID, QUANTITY);

        // Then
        assertEquals(5, existingItem.getQuantity()); // 3 + 2
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemFromCart_ValidParameters_DeletesItem() {
        // Given
        Cart cart = new Cart();
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(cart));

        // When
        cartService.removeItemFromCart(USER_ID, PRODUCT_ID);

        // Then
        verify(cartItemRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);
        verify(cartRepository).findById(USER_ID);
    }

    @Test
    void getCartItems_WithValidOffset_ReturnsItems() {
        // Given
        int offset = 0;
        Cart cart = new Cart();
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(cart));

        List<CartItem> expectedItems = List.of(new CartItem(), new CartItem());
        when(cartItemRepository.findByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(expectedItems);

        // When
        List<CartItem> result = cartService.getCartItems(USER_ID, offset);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cartItemRepository).findByUserId(eq(USER_ID), any(Pageable.class));
    }

    @Test
    void getCartItems_WithNegativeOffset_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                cartService.getCartItems(USER_ID, -1));
    }

    @Test
    void getCartSize_ValidUserId_ReturnsSize() {
        // Given
        Cart cart = new Cart();
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.sumQuantityByUserId(USER_ID)).thenReturn(5);

        // When
        int result = cartService.getCartSize(USER_ID);

        // Then
        assertEquals(5, result);
        verify(cartItemRepository).sumQuantityByUserId(USER_ID);
    }

    @Test
    void updateProductQuantity_IncreaseQuantity_ReturnsNewQuantity() {
        // Given
        CartItem cartItem = new CartItem();
        cartItem.setQuantity(3);
        when(cartItemRepository.findByCartUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        // When
        int result = cartService.updateProductQuantity(USER_ID, PRODUCT_ID, true);

        // Then
        assertEquals(4, result);
        assertEquals(4, cartItem.getQuantity());
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateProductQuantity_DecreaseToPositive_ReturnsNewQuantity() {
        // Given
        CartItem cartItem = new CartItem();
        cartItem.setQuantity(3);
        when(cartItemRepository.findByCartUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        // When
        int result = cartService.updateProductQuantity(USER_ID, PRODUCT_ID, false);

        // Then
        assertEquals(2, result);
        assertEquals(2, cartItem.getQuantity());
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateProductQuantity_DecreaseToZero_DeletesItemAndReturnsZero() {
        // Given
        CartItem cartItem = new CartItem();
        cartItem.setQuantity(1);
        when(cartItemRepository.findByCartUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);

        // When
        int result = cartService.updateProductQuantity(USER_ID, PRODUCT_ID, false);

        // Then
        assertEquals(0, result);
        verify(cartItemRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateProductQuantity_ItemNotFound_ReturnsZero() {
        // Given
        when(cartItemRepository.findByCartUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

        // When
        int result = cartService.updateProductQuantity(USER_ID, PRODUCT_ID, true);

        // Then
        assertEquals(0, result);
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartItemRepository, never()).deleteByUserIdAndProductId(anyLong(), anyLong());
    }

    @Test
    void getProductIdsByUserId_ValidUser_ReturnsProductIds() {
        // Given
        List<Long> expectedIds = List.of(100L, 200L, 300L);
        when(cartItemRepository.findProductIdsByUserId(USER_ID)).thenReturn(expectedIds);

        // When
        List<Long> result = cartService.getProductIdsByUserId(USER_ID);

        // Then
        assertEquals(expectedIds, result);
        verify(cartItemRepository).findProductIdsByUserId(USER_ID);
    }

    @Test
    void productExists_ProductInCart_ReturnsTrue() {
        // Given
        when(cartItemRepository.existsByCartUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(true);

        // When
        boolean result = cartService.productExists(PRODUCT_ID, USER_ID);

        // Then
        assertTrue(result);
        verify(cartItemRepository).existsByCartUserIdAndProductId(USER_ID, PRODUCT_ID);
    }

    @Test
    void getBriefItems_ValidUser_ReturnsBriefItems() {
        // Given
        CartItem item1 = new CartItem();
        item1.setProductId(100L);
        item1.setQuantity(2);

        CartItem item2 = new CartItem();
        item2.setProductId(200L);
        item2.setQuantity(1);

        List<CartItem> cartItems = List.of(item1, item2);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(cartItems);

        // When
        List<ProductIdAndQuantityDto> result = cartService.getBriefItems(USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getProductId());
        assertEquals(2, result.get(0).getProductQuantity());
        assertEquals(200L, result.get(1).getProductId());
        assertEquals(1, result.get(1).getProductQuantity());
    }

    @Test
    void addItemsToCart_WithAvailableSpace_AddsItems() {
        // Given
        Cart cart = new Cart();
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.sumQuantityByUserId(USER_ID)).thenReturn(10); // 3990 available

        List<ProductIdAndQuantityDto> productDTOs = List.of(
                new ProductIdAndQuantityDto(100L, 5),
                new ProductIdAndQuantityDto(200L, 3)
        );

        when(productGrpcClient.productsExist(anyList())).thenReturn(List.of(100L, 200L));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int result = cartService.addItemsToCart(USER_ID, productDTOs);

        // Then
        assertEquals(8, result); // 5 + 3
        verify(cartItemRepository, times(2)).save(any(CartItem.class));
    }

    @Test
    void addItemsToCart_WithLimitedSpace_AddsPartialItems() {
        // Given
        Cart cart = new Cart();
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.sumQuantityByUserId(USER_ID)).thenReturn(3995); // Only 5 available

        List<ProductIdAndQuantityDto> productDTOs = List.of(
                new ProductIdAndQuantityDto(100L, 10)
        );

        when(productGrpcClient.productsExist(anyList())).thenReturn(List.of(100L));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int result = cartService.addItemsToCart(USER_ID, productDTOs);

        // Then
        assertEquals(5, result); // Only 5 added due to space limit
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void removeItemsFromCart_ValidParameters_DeletesItems() {
        // Given
        Cart cart = new Cart();
        when(cartRepository.findById(USER_ID)).thenReturn(Optional.of(cart));
        List<Long> productIds = List.of(100L, 200L);

        // When
        cartService.removeItemsFromCart(USER_ID, productIds);

        // Then
        verify(cartItemRepository).deleteByUserIdAndProductIdIn(USER_ID, productIds);
    }




}
