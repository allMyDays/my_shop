package com.example.cart_wishlist.unit.controller;

import com.example.cart_wishlist.controller.rest.CartRestController;
import com.example.cart_wishlist.entity.CartItem;
import com.example.cart_wishlist.exception.TooManyItemsException;
import com.example.cart_wishlist.mapper.CartItemMapper;
import com.example.cart_wishlist.mapper.CartMapper;
import com.example.cart_wishlist.service.CartService;
import com.example.common.dto.cart.rest.CartItemResponseDTO;
import com.example.common.dto.cart.rest.CartResponseDTO;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartRestControllerUnitTest {

    @Mock
    private CartService cartService;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private CartRestController cartRestController;

    private final Long TEST_USER_ID = 123L;
    private final Long TEST_PRODUCT_ID = 456L;

    @BeforeEach
    void setUp() {
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
    }

    // Tests for getCart method
    @Test
    void getCart_WhenValidRequest_ShouldReturnCart() throws UserNotFoundException {
        // Arrange
        CartResponseDTO expectedCart = new CartResponseDTO();
        when(cartMapper.toCartResponseDTO(anyLong(), anyList())).thenReturn(expectedCart);

        // Act
        CartResponseDTO result = cartRestController.getCart(jwt);

        // Assert
        assertEquals(expectedCart, result);
        verify(cartMapper).toCartResponseDTO(anyLong(), anyList());
    }

    @Test
    void getCart_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        when(cartMapper.toCartResponseDTO(anyLong(), anyList()))
                .thenThrow(new UserNotFoundException());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            cartRestController.getCart(jwt);
        });
    }

    // Tests for updateItemQuantityByOne method
    @Test
    void updateItemQuantityByOne_WhenIncreaseTrue_ShouldReturnNewQuantity() throws UserNotFoundException {
        // Arrange
        boolean increase = true;
        Integer pricePerProduct = 1000;
        int newQuantity = 5;

        when(cartService.updateProductQuantity(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID), eq(increase)))
                .thenReturn(newQuantity);

        // Act
        ResponseEntity<?> response = cartRestController.updateItemQuantityByOne(TEST_PRODUCT_ID, increase, pricePerProduct, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(newQuantity, responseBody.get("newQuantity"));
        assertEquals("5 000₽", responseBody.get("totalPriceView")); // 1000 * 5 = 5000 -> "5 000"

        verify(cartService).updateProductQuantity(TEST_USER_ID, TEST_PRODUCT_ID, increase);
    }

    @Test
    void updateItemQuantityByOne_WhenIncreaseFalse_ShouldReturnNewQuantity() throws UserNotFoundException {
        // Arrange
        boolean increase = false;
        Integer pricePerProduct = 500;
        int newQuantity = 2;

        when(cartService.updateProductQuantity(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID), eq(increase)))
                .thenReturn(newQuantity);

        // Act
        ResponseEntity<?> response = cartRestController.updateItemQuantityByOne(TEST_PRODUCT_ID, increase, pricePerProduct, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(newQuantity, responseBody.get("newQuantity"));
        assertEquals("1 000₽", responseBody.get("totalPriceView")); // 500 * 2 = 1000 -> "1 000"
    }

    @Test
    void updateItemQuantityByOne_WhenPricePerProductIsNull_ShouldNotIncludeTotalPrice() throws UserNotFoundException {
        // Arrange
        boolean increase = true;
        Integer pricePerProduct = null;
        int newQuantity = 3;

        when(cartService.updateProductQuantity(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID), eq(increase)))
                .thenReturn(newQuantity);

        // Act
        ResponseEntity<?> response = cartRestController.updateItemQuantityByOne(TEST_PRODUCT_ID, increase, pricePerProduct, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(newQuantity, responseBody.get("newQuantity"));
        assertFalse(responseBody.containsKey("totalPriceView"));
    }

    @Test
    void updateItemQuantityByOne_WhenQuantityIsZero_ShouldHandleZeroQuantity() throws UserNotFoundException {
        // Arrange
        boolean increase = false;
        Integer pricePerProduct = 1000;
        int newQuantity = 0; // товар удален

        when(cartService.updateProductQuantity(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID), eq(increase)))
                .thenReturn(newQuantity);

        // Act
        ResponseEntity<?> response = cartRestController.updateItemQuantityByOne(TEST_PRODUCT_ID, increase, pricePerProduct, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(0, responseBody.get("newQuantity"));
        assertEquals("0₽", responseBody.get("totalPriceView")); // 1000 * 0 = 0
    }

    // Tests for getCartItems method
    @Test
    void getCartItems_WhenValidRequest_ShouldReturnItems() throws UserNotFoundException {
        // Arrange
        int offset = 0;
        List<CartItem> cartItems = List.of(new CartItem(), new CartItem());
        List<CartItemResponseDTO> expectedDtos = List.of(new CartItemResponseDTO(), new CartItemResponseDTO());

        when(cartService.getCartItems(eq(TEST_USER_ID), eq(offset))).thenReturn(cartItems);
        when(cartItemMapper.mapCartItems(eq(cartItems))).thenReturn(expectedDtos);

        // Act
        List<CartItemResponseDTO> result = cartRestController.getCartItems(offset, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(cartService).getCartItems(TEST_USER_ID, offset);
        verify(cartItemMapper).mapCartItems(cartItems);
    }

    @Test
    void getCartItems_WithOffset_ShouldPassCorrectOffset() throws UserNotFoundException {
        // Arrange
        int offset = 40;
        List<CartItem> cartItems = List.of(new CartItem());
        List<CartItemResponseDTO> expectedDtos = List.of(new CartItemResponseDTO());

        when(cartService.getCartItems(eq(TEST_USER_ID), eq(offset))).thenReturn(cartItems);
        when(cartItemMapper.mapCartItems(eq(cartItems))).thenReturn(expectedDtos);

        // Act
        List<CartItemResponseDTO> result = cartRestController.getCartItems(offset, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(cartService).getCartItems(TEST_USER_ID, 40);
    }

    // Tests for getCartSize method
    @Test
    void getCartSize_ShouldReturnSize() throws UserNotFoundException {
        // Arrange
        int expectedSize = 5;
        when(cartService.getCartSize(eq(TEST_USER_ID))).thenReturn(expectedSize);

        // Act
        Integer result = cartRestController.getCartSize(jwt);

        // Assert
        assertEquals(expectedSize, result);
        verify(cartService).getCartSize(TEST_USER_ID);
    }

    @Test
    void getCartSize_WhenCartIsEmpty_ShouldReturnZero() throws UserNotFoundException {
        // Arrange
        when(cartService.getCartSize(eq(TEST_USER_ID))).thenReturn(0);

        // Act
        Integer result = cartRestController.getCartSize(jwt);

        // Assert
        assertEquals(0, result);
    }

    // Tests for addToCart method
    @Test
    void addToCart_WhenValidRequest_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        int quantity = 2;
        doNothing().when(cartService).addItemToCart(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID), eq(quantity));

        // Act
        ResponseEntity<?> response = cartRestController.addToCart(TEST_PRODUCT_ID, quantity, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService).addItemToCart(TEST_USER_ID, TEST_PRODUCT_ID, quantity);
    }

    @Test
    void addToCart_WhenTooManyItems_ShouldReturnTooManyRequests() throws UserNotFoundException {
        // Arrange
        int quantity = 100;
        doThrow(new TooManyItemsException(true))
                .when(cartService).addItemToCart(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID), eq(quantity));

        // Act
        ResponseEntity<?> response = cartRestController.addToCart(TEST_PRODUCT_ID, quantity, jwt);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Вы превысили лимит на добавление товаров в корзину. Удалите часть товаров, чтобы освободить место.", response.getBody());
        verify(cartService).addItemToCart(TEST_USER_ID, TEST_PRODUCT_ID, quantity);
    }

    // Tests for removeFromCart method
    @Test
    void removeFromCart_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        doNothing().when(cartService).removeItemFromCart(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID));

        // Act
        ResponseEntity<?> response = cartRestController.removeFromCart(TEST_PRODUCT_ID, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService).removeItemFromCart(TEST_USER_ID, TEST_PRODUCT_ID);
    }

    // Tests for getProductIDs method
    @Test
    void getProductIDs_ShouldReturnProductIds() throws UserNotFoundException {
        // Arrange
        List<Long> expectedIds = List.of(1L, 2L, 3L);
        when(cartService.getProductIdsByUserId(eq(TEST_USER_ID))).thenReturn(expectedIds);

        // Act
        List<Long> result = cartRestController.getProductIDs(jwt);

        // Assert
        assertEquals(expectedIds, result);
        verify(cartService).getProductIdsByUserId(TEST_USER_ID);
    }

    @Test
    void getProductIDs_WhenCartIsEmpty_ShouldReturnEmptyList() throws UserNotFoundException {
        // Arrange
        List<Long> emptyList = List.of();
        when(cartService.getProductIdsByUserId(eq(TEST_USER_ID))).thenReturn(emptyList);

        // Act
        List<Long> result = cartRestController.getProductIDs(jwt);

        // Assert
        assertTrue(result.isEmpty());
    }

    // Tests for isProductInCart method
    @Test
    void isProductInCart_WhenProductExists_ShouldReturnTrue() throws UserNotFoundException {
        // Arrange
        when(cartService.productExists(eq(TEST_PRODUCT_ID), eq(TEST_USER_ID))).thenReturn(true);

        // Act
        boolean result = cartRestController.isProductInCart(TEST_PRODUCT_ID, jwt);

        // Assert
        assertTrue(result);
        verify(cartService).productExists(TEST_PRODUCT_ID, TEST_USER_ID);
    }

    @Test
    void isProductInCart_WhenProductNotExists_ShouldReturnFalse() throws UserNotFoundException {
        // Arrange
        when(cartService.productExists(eq(TEST_PRODUCT_ID), eq(TEST_USER_ID))).thenReturn(false);

        // Act
        boolean result = cartRestController.isProductInCart(TEST_PRODUCT_ID, jwt);

        // Assert
        assertFalse(result);
    }

    // Tests for getBriefItems method
    @Test
    void getBriefItems_ShouldReturnBriefItems() throws UserNotFoundException {
        // Arrange
        List<ProductIdAndQuantityDto> expectedItems = List.of(
                new ProductIdAndQuantityDto(1L, 2),
                new ProductIdAndQuantityDto(2L, 1)
        );
        when(cartService.getBriefItems(eq(TEST_USER_ID))).thenReturn(expectedItems);

        // Act
        List<ProductIdAndQuantityDto> result = cartRestController.getBriefItems(jwt);

        // Assert
        assertEquals(expectedItems, result);
        verify(cartService).getBriefItems(TEST_USER_ID);
    }


}