package com.example.cart_wishlist.unit.service;

import com.example.cart_wishlist.entity.WishItem;
import com.example.cart_wishlist.entity.WishList;
import com.example.cart_wishlist.exception.TooManyItemsException;
import com.example.cart_wishlist.repository.WishItemRepository;
import com.example.cart_wishlist.repository.WishListRepository;
import com.example.cart_wishlist.service.WishListService;
import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishListServiceUnitTest {

    @Mock
    private WishListRepository wishListRepository;

    @Mock
    private WishItemRepository wishItemRepository;

    @Mock
    private ProductGrpcClient productGrpcClient;

    @InjectMocks
    private WishListService wishListService;

    private final Long USER_ID = 1L;
    private final Long PRODUCT_ID = 100L;

    @Test
    void getOrCreateWishList_WhenWishListExists_ReturnsExistingWishList() {
        // Given
        WishList expectedWishList = new WishList();
        expectedWishList.setUserId(USER_ID);
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(expectedWishList));

        // When
        WishList result = wishListService.getOrCreateWishList(USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        verify(wishListRepository).findById(USER_ID);
        verify(wishListRepository, never()).save(any(WishList.class));
    }

    @Test
    void getOrCreateWishList_WhenWishListNotExists_CreatesNewWishList() {
        // Given
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.empty());

        WishList newWishList = new WishList();
        newWishList.setUserId(USER_ID);
        when(wishListRepository.save(any(WishList.class))).thenReturn(newWishList);

        // When
        WishList result = wishListService.getOrCreateWishList(USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        verify(wishListRepository).findById(USER_ID);
        verify(wishListRepository).save(any(WishList.class));
    }

    @Test
    void addItem_WhenWishListIsFull_ThrowsTooManyItemsException() {
        // Given
        when(wishItemRepository.countQuantityByUserId(USER_ID)).thenReturn(8000L);

        // When & Then
        TooManyItemsException exception = assertThrows(TooManyItemsException.class,
                () -> wishListService.addItem(USER_ID, PRODUCT_ID));

        assertFalse(exception.isCart());
        verify(productGrpcClient, never()).productExists(anyLong());
    }

    @Test
    void addItem_WhenProductNotExists_ThrowsProductNotFoundException() {
        // Given
        when(wishItemRepository.countQuantityByUserId(USER_ID)).thenReturn(10L);
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(false);

        // When & Then
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> wishListService.addItem(USER_ID, PRODUCT_ID));

        assertEquals(List.of(PRODUCT_ID), exception.getProductIds());
        verify(productGrpcClient).productExists(PRODUCT_ID);
        verify(wishItemRepository, never()).findByWishListUserIdAndProductId(anyLong(), anyLong());
    }

    @Test
    void addItem_WhenProductExistsAndItemNotInWishList_AddsNewItem() {
        // Given
        when(wishItemRepository.countQuantityByUserId(USER_ID)).thenReturn(10L);
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(true);

        WishList wishList = new WishList();
        wishList.setUserId(USER_ID);
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(wishList));
        when(wishItemRepository.findByWishListUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

        // When
        wishListService.


                addItem(USER_ID, PRODUCT_ID);

        // Then
        verify(wishItemRepository).save(any(WishItem.class));
        verify(wishItemRepository).findByWishListUserIdAndProductId(USER_ID, PRODUCT_ID);
    }

    @Test
    void addItem_WhenProductExistsAndItemAlreadyInWishList_DoesNothing() {
        // Given
        when(wishItemRepository.countQuantityByUserId(USER_ID)).thenReturn(10L);
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(true);

        WishList wishList = new WishList();
        wishList.setUserId(USER_ID);
        WishItem existingItem = new WishItem();

        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(wishList));
        when(wishItemRepository.findByWishListUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(existingItem));

        // When
        wishListService.addItem(USER_ID, PRODUCT_ID);

        // Then
        verify(wishItemRepository, never()).save(any(WishItem.class));
        verify(wishItemRepository).findByWishListUserIdAndProductId(USER_ID, PRODUCT_ID);
    }

    @Test
    void removeItem_ValidParameters_DeletesItem() {
        // Given
        WishList wishList = new WishList();
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(wishList));

        // When
        wishListService.removeItem(USER_ID, PRODUCT_ID);

        // Then
        verify(wishItemRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);
        verify(wishListRepository).findById(USER_ID);
    }

    @Test
    void getItems_WithValidOffset_ReturnsItems() {
        // Given
        int offset = 0;
        WishList wishList = new WishList();
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(wishList));

        List<WishItem> expectedItems = List.of(new WishItem(), new WishItem());
        when(wishItemRepository.findAllByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(expectedItems);

        // When
        List<WishItem> result = wishListService.getItems(USER_ID, offset);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(wishItemRepository).findAllByUserId(eq(USER_ID), any(Pageable.class));
    }

    @Test
    void getItems_WithOffset40_UsesCorrectPageable() {
        // Given
        int offset = 40;
        WishList wishList = new WishList();
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(wishList));

        List<WishItem> expectedItems = List.of(new WishItem());
        when(wishItemRepository.findAllByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(expectedItems);

        // When
        List<WishItem> result = wishListService.getItems(USER_ID, offset);

        // Then
        assertNotNull(result);
        verify(wishItemRepository).findAllByUserId(eq(USER_ID), argThat(pageable ->
                ((PageRequest) pageable).getPageNumber() == 1 &&
                        ((PageRequest) pageable).getPageSize() == 40
        ));
    }

    @Test
    void getItems_WithNegativeOffset_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> wishListService.getItems(USER_ID, -1));

        assertEquals("offset must be greater or equal to 0", exception.getMessage());
    }

    @Test
    void getListSize_ValidUserId_ReturnsSize() {
        // Given
        WishList wishList = new WishList();
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(wishList));
        when(wishItemRepository.countQuantityByUserId(USER_ID)).thenReturn(15L);

        // When
        long result = wishListService.getListSize(USER_ID);

        // Then
        assertEquals(15L, result);
        verify(wishItemRepository).countQuantityByUserId(USER_ID);
    }

    @Test
    void getProductIdsByUserId_ValidUser_ReturnsProductIds() {
        // Given
        List<Long> expectedIds = List.of(100L, 200L, 300L);
        when(wishItemRepository.findProductIdsByUserId(USER_ID)).thenReturn(expectedIds);

        // When
        List<Long> result = wishListService.getProductIdsByUserId(USER_ID);

        // Then
        assertEquals(expectedIds, result);
        verify(wishItemRepository).findProductIdsByUserId(USER_ID);
    }

    @Test
    void productExists_ProductInWishList_ReturnsTrue() {
        // Given
        when(wishItemRepository.existsByWishListUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(true);

        // When
        boolean result = wishListService.productExists(PRODUCT_ID, USER_ID);

        // Then
        assertTrue(result);
        verify(wishItemRepository).existsByWishListUserIdAndProductId(USER_ID, PRODUCT_ID);
    }

    @Test
    void productExists_ProductNotInWishList_ReturnsFalse() {
        // Given
        when(wishItemRepository.existsByWishListUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(false);

        // When
        boolean result = wishListService.productExists(PRODUCT_ID, USER_ID);

        // Then
        assertFalse(result);
        verify(wishItemRepository).existsByWishListUserIdAndProductId(USER_ID, PRODUCT_ID);
    }

    @Test
    void addItem_WhenWishListIsAlmostFull_AddsItemSuccessfully() {
        // Given
        when(wishItemRepository.countQuantityByUserId(USER_ID)).thenReturn(7999L);
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(true);

        WishList wishList = new WishList();
        wishList.setUserId(USER_ID);
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(wishList));
        when(wishItemRepository.findByWishListUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

        // When
        wishListService.addItem(USER_ID, PRODUCT_ID);

        // Then
        verify(wishItemRepository).save(any(WishItem.class));
    }

    @Test
    void removeItem_WhenWishListNotExists_CreatesWishListBeforeDeletion() {
        // Given
        WishList newWishList = new WishList();
        newWishList.setUserId(USER_ID);
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(wishListRepository.save(any(WishList.class))).thenReturn(newWishList);

        // When
        wishListService.removeItem(USER_ID, PRODUCT_ID);

        // Then
        verify(wishListRepository).save(any(WishList.class));
        verify(wishItemRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);
    }

    @Test
    void getItems_WithLargeOffset_CalculatesCorrectPage() {
        // Given
        int offset = 120; // Should be page 3 (120 / 40 = 3)
        WishList wishList = new WishList();
        when(wishListRepository.findById(USER_ID)).thenReturn(Optional.of(wishList));

        List<WishItem> expectedItems = List.of(new WishItem());
        when(wishItemRepository.findAllByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(expectedItems);

        // When
        List<WishItem> result = wishListService.getItems(USER_ID, offset);

        // Then
        assertNotNull(result);
        verify(wishItemRepository).findAllByUserId(eq(USER_ID), argThat(pageable ->
                ((PageRequest) pageable).getPageNumber() == 3
        ));
    }
}