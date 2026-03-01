package com.example.cart_wishlist.unit.controller;

import com.example.cart_wishlist.controller.rest.WishListRestController;
import com.example.cart_wishlist.entity.WishItem;
import com.example.cart_wishlist.exception.TooManyItemsException;
import com.example.cart_wishlist.mapper.WishItemMapper;
import com.example.cart_wishlist.mapper.WishListMapper;
import com.example.cart_wishlist.service.WishListService;
import com.example.cart_wishlist.dto.WishItemResponseDTO;
import com.example.cart_wishlist.dto.WishListResponseDTO;
import com.example.common.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishListRestUnitControllerTest {

    @Mock
    private WishListService wishService;

    @Mock
    private WishListMapper listMapper;

    @Mock
    private WishItemMapper itemMapper;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private WishListRestController wishListRestController;

    private final Long TEST_USER_ID = 123L;
    private final Long TEST_PRODUCT_ID = 456L;

    @BeforeEach
    void setUp() {
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
    }

    // Tests for getWishList method
    @Test
    void getWishList_WhenValidRequest_ShouldReturnWishList() throws UserNotFoundException {
        // Arrange
        WishListResponseDTO expectedWishList = new WishListResponseDTO();
        when(listMapper.toWishListResponseDTO(anyLong(), anyList())).thenReturn(expectedWishList);

        // Act
        WishListResponseDTO result = wishListRestController.getWishList(jwt);

        // Assert
        assertEquals(expectedWishList, result);
        verify(listMapper).toWishListResponseDTO(anyLong(), anyList());
    }

    @Test
    void getWishList_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        when(listMapper.toWishListResponseDTO(anyLong(), anyList()))
                .thenThrow(new UserNotFoundException());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            wishListRestController.getWishList(jwt);
        });
    }

    // Tests for getItems method
    @Test
    void getItems_WhenValidRequest_ShouldReturnItems() throws UserNotFoundException {
        // Arrange
        int offset = 0;
        List<WishItem> wishItems = List.of(new WishItem(), new WishItem());
        List<WishItemResponseDTO> expectedDtos = List.of(new WishItemResponseDTO(), new WishItemResponseDTO());

        when(wishService.getItems(eq(TEST_USER_ID), eq(offset))).thenReturn(wishItems);
        when(itemMapper.mapItems(eq(wishItems))).thenReturn(expectedDtos);

        // Act
        List<WishItemResponseDTO> result = wishListRestController.getItems(offset, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(wishService).getItems(TEST_USER_ID, offset);
        verify(itemMapper).mapItems(wishItems);
    }

    @Test
    void getItems_WithOffset_ShouldPassCorrectOffset() throws UserNotFoundException {
        // Arrange
        int offset = 40;
        List<WishItem> wishItems = List.of(new WishItem());
        List<WishItemResponseDTO> expectedDtos = List.of(new WishItemResponseDTO());

        when(wishService.getItems(eq(TEST_USER_ID), eq(offset))).thenReturn(wishItems);
        when(itemMapper.mapItems(eq(wishItems))).thenReturn(expectedDtos);

        // Act
        List<WishItemResponseDTO> result = wishListRestController.getItems(offset, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(wishService).getItems(TEST_USER_ID, 40);
    }

    @Test
    void getItems_WhenNoItems_ShouldReturnEmptyList() throws UserNotFoundException {
        // Arrange
        int offset = 0;
        List<WishItem> emptyItems = List.of();
        List<WishItemResponseDTO> emptyDtos = List.of();

        when(wishService.getItems(eq(TEST_USER_ID), eq(offset))).thenReturn(emptyItems);
        when(itemMapper.mapItems(eq(emptyItems))).thenReturn(emptyDtos);

        // Act
        List<WishItemResponseDTO> result = wishListRestController.getItems(offset, jwt);

        // Assert
        assertTrue(result.isEmpty());
        verify(wishService).getItems(TEST_USER_ID, offset);
    }

    // Tests for getListSize method
    @Test
    void getListSize_ShouldReturnSize() throws UserNotFoundException {
        // Arrange
        Long expectedSize = 15L;
        when(wishService.getListSize(eq(TEST_USER_ID))).thenReturn(expectedSize);

        // Act
        Long result = wishListRestController.getListSize(jwt);

        // Assert
        assertEquals(expectedSize, result);
        verify(wishService).getListSize(TEST_USER_ID);
    }

    @Test
    void getListSize_WhenListIsEmpty_ShouldReturnZero() throws UserNotFoundException {
        // Arrange
        when(wishService.getListSize(eq(TEST_USER_ID))).thenReturn(0L);

        // Act
        Long result = wishListRestController.getListSize(jwt);

        // Assert
        assertEquals(0L, result);
    }

    // Tests for addToWishList method
    @Test
    void addToWishList_WhenValidRequest_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        doNothing().when(wishService).addItem(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID));

        // Act
        ResponseEntity<?> response = wishListRestController.addToWishList(TEST_PRODUCT_ID, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(wishService).addItem(TEST_USER_ID, TEST_PRODUCT_ID);
    }

    @Test
    void addToWishList_WhenTooManyItems_ShouldReturnTooManyRequests() throws UserNotFoundException {
        // Arrange
        doThrow(new TooManyItemsException(false))
                .when(wishService).addItem(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID));

        // Act
        ResponseEntity<?> response = wishListRestController.addToWishList(TEST_PRODUCT_ID, jwt);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Вы превысили лимит на добавление товаров в список желаний. Удалите часть товаров, чтобы освободить место.", response.getBody());
        verify(wishService).addItem(TEST_USER_ID, TEST_PRODUCT_ID);
    }

    @Test
    void addToWishList_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        doThrow(new UserNotFoundException())
                .when(wishService).addItem(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            wishListRestController.addToWishList(TEST_PRODUCT_ID, jwt);
        });
    }

    // Tests for removeFromWishList method
    @Test
    void removeFromWishList_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        doNothing().when(wishService).removeItem(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID));

        // Act
        ResponseEntity<?> response = wishListRestController.removeFromWishList(TEST_PRODUCT_ID, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(wishService).removeItem(TEST_USER_ID, TEST_PRODUCT_ID);
    }

    @Test
    void removeFromWishList_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        doThrow(new UserNotFoundException())
                .when(wishService).removeItem(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            wishListRestController.removeFromWishList(TEST_PRODUCT_ID, jwt);
        });
    }

    // Tests for getProductIDs method
    @Test
    void getProductIDs_ShouldReturnProductIds() throws UserNotFoundException {
        // Arrange
        List<Long> expectedIds = List.of(1L, 2L, 3L, 4L);
        when(wishService.getProductIdsByUserId(eq(TEST_USER_ID))).thenReturn(expectedIds);

        // Act
        List<Long> result = wishListRestController.getProductIDs(jwt);

        // Assert
        assertEquals(expectedIds, result);
        verify(


                wishService).getProductIdsByUserId(TEST_USER_ID);
    }

    @Test
    void getProductIDs_WhenWishListIsEmpty_ShouldReturnEmptyList() throws UserNotFoundException {
        // Arrange
        List<Long> emptyList = List.of();
        when(wishService.getProductIdsByUserId(eq(TEST_USER_ID))).thenReturn(emptyList);

        // Act
        List<Long> result = wishListRestController.getProductIDs(jwt);

        // Assert
        assertTrue(result.isEmpty());
    }

    // Tests for isProductInWishList method
    @Test
    void isProductInWishList_WhenProductExists_ShouldReturnTrue() throws UserNotFoundException {
        // Arrange
        when(wishService.productExists(eq(TEST_PRODUCT_ID), eq(TEST_USER_ID))).thenReturn(true);

        // Act
        boolean result = wishListRestController.isProductInWishList(TEST_PRODUCT_ID, jwt);

        // Assert
        assertTrue(result);
        verify(wishService).productExists(TEST_PRODUCT_ID, TEST_USER_ID);
    }

    @Test
    void isProductInWishList_WhenProductNotExists_ShouldReturnFalse() throws UserNotFoundException {
        // Arrange
        when(wishService.productExists(eq(TEST_PRODUCT_ID), eq(TEST_USER_ID))).thenReturn(false);

        // Act
        boolean result = wishListRestController.isProductInWishList(TEST_PRODUCT_ID, jwt);

        // Assert
        assertFalse(result);
    }

    @Test
    void isProductInWishList_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        when(wishService.productExists(eq(TEST_PRODUCT_ID), eq(TEST_USER_ID)))
                .thenThrow(new UserNotFoundException());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            wishListRestController.isProductInWishList(TEST_PRODUCT_ID, jwt);
        });
    }

}
