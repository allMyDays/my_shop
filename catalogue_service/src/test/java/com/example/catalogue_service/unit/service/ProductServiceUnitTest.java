package com.example.catalogue_service.unit.service;

import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.repository.ProductRepository;
import com.example.catalogue_service.service.ProductService;
import com.example.common.dto.product.ProductIdAndPriceDto;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private final Long PRODUCT_ID = 100L;
    private final Long CATEGORY_ID = 1L;
    private final String TITLE = "Test Product";

    @Test
    void getAll_WithValidCategoryAndTitle_ReturnsStream() {
        // Given
        int offset = 0;
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setTitle(TITLE);

        when(productRepository.findByTitleAndOptionalCategory(eq(TITLE), eq(CATEGORY_ID), any(PageRequest.class)))
                .thenReturn(Stream.of(product));

        // When
        Stream<Product> result = productService.getAll(CATEGORY_ID, TITLE, offset);

        // Then
        assertNotNull(result);
        assertEquals(1, result.count());
        verify(productRepository).findByTitleAndOptionalCategory(eq(TITLE), eq(CATEGORY_ID),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 40));
    }

    @Test
    void getAll_WithNullCategory_ConvertsToNull() {
        // Given
        when(productRepository.findByTitleAndOptionalCategory(eq(TITLE), isNull(), any(PageRequest.class)))
                .thenReturn(Stream.empty());

        // When
        Stream<Product> result = productService.getAll(null, TITLE, 0);

        // Then
        assertNotNull(result);
        verify(productRepository).findByTitleAndOptionalCategory(eq(TITLE), isNull(), any(PageRequest.class));
    }

    @Test
    void getAll_WithZeroCategory_ConvertsToNull() {
        // Given
        when(productRepository.findByTitleAndOptionalCategory(eq(TITLE), isNull(), any(PageRequest.class)))
                .thenReturn(Stream.empty());

        // When
        Stream<Product> result = productService.getAll(0L, TITLE, 0);

        // Then
        assertNotNull(result);
        verify(productRepository).findByTitleAndOptionalCategory(eq(TITLE), isNull(), any(PageRequest.class));
    }

    @Test
    void getAll_WithShortTitle_ReturnsEmptyStream() {
        // When
        Stream<Product> result = productService.getAll(CATEGORY_ID, "A", 0);

        // Then
        assertNotNull(result);
        assertEquals(0, result.count());
        verify(productRepository, never()).findByTitleAndOptionalCategory(anyString(), anyLong(), any(PageRequest.class));
    }

    @Test
    void getAll_WithEmptyTitle_ReturnsEmptyStream() {
        // When
        Stream<Product> result = productService.getAll(CATEGORY_ID, "", 0);

        // Then
        assertNotNull(result);
        assertEquals(0, result.count());
        verify(productRepository, never()).findByTitleAndOptionalCategory(anyString(), anyLong(), any(PageRequest.class));
    }

    @Test
    void getAll_WithOffset40_UsesCorrectPage() {
        // Given
        when(productRepository.findByTitleAndOptionalCategory(eq(TITLE), eq(CATEGORY_ID), any(PageRequest.class)))
                .thenReturn(Stream.empty());

        // When
        productService.getAll(CATEGORY_ID, TITLE, 40);

        // Then
        verify(productRepository).findByTitleAndOptionalCategory(eq(TITLE), eq(CATEGORY_ID),
                argThat(page -> page.getPageNumber() == 1 && page.getPageSize() == 40));
    }

    @Test
    void getProductsByIDs_WithValidIds_ReturnsStream() {
        // Given
        List<Long> productIds = List.of(100L, 200L, 300L);
        Product product1 = new Product();
        product1.setId(100L);
        Product


                product2 = new Product();
        product2.setId(200L);

        when(productRepository.findAllByIdIn(productIds))
                .thenReturn(Stream.of(product1, product2));

        // When
        Stream<Product> result = productService.getProductsByIDs(productIds);

        // Then
        assertNotNull(result);
        assertEquals(2, result.count());
        verify(productRepository).findAllByIdIn(productIds);
    }

    @Test
    void getProductsByIDs_WithEmptyList_ReturnsEmptyStream() {
        // Given
        when(productRepository.findAllByIdIn(List.of())).thenReturn(Stream.empty());

        // When
        Stream<Product> result = productService.getProductsByIDs(List.of());

        // Then
        assertNotNull(result);
        assertEquals(0, result.count());
        verify(productRepository).findAllByIdIn(List.of());
    }

    @Test
    void getProductByID_ProductExists_ReturnsProduct() {
        // Given
        Product product = new Product();
        product.setId(PRODUCT_ID);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // When
        Optional<Product> result = productService.getProductByID(PRODUCT_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(PRODUCT_ID, result.get().getId());
        verify(productRepository).findById(PRODUCT_ID);
    }

    @Test
    void getProductByID_ProductNotExists_ReturnsEmpty() {
        // Given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.getProductByID(PRODUCT_ID);

        // Then
        assertFalse(result.isPresent());
        verify(productRepository).findById(PRODUCT_ID);
    }

    @Test
    void productExists_ProductExists_ReturnsTrue() {
        // Given
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);

        // When
        boolean result = productService.productExists(PRODUCT_ID);

        // Then
        assertTrue(result);
        verify(productRepository).existsById(PRODUCT_ID);
    }

    @Test
    void productExists_ProductNotExists_ReturnsFalse() {
        // Given
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(false);

        // When
        boolean result = productService.productExists(PRODUCT_ID);

        // Then
        assertFalse(result);
        verify(productRepository).existsById(PRODUCT_ID);
    }

    @Test
    void productsExist_WithValidIds_ReturnsExistingIds() {
        // Given
        List<Long> inputIds = List.of(100L, 200L, -1L, 300L);
        List<Long> expectedIds = List.of(100L, 200L, 300L);
        List<Long> existingIds = List.of(100L, 300L);

        when(productRepository.findProductIdsByIdIn(expectedIds)).thenReturn(existingIds);

        // When
        List<Long> result = productService.productsExist(inputIds);

        // Then
        assertEquals(existingIds, result);
        verify(productRepository).findProductIdsByIdIn(expectedIds);
    }

    @Test
    void productsExist_WithNegativeIds_FiltersThemOut() {
        // Given
        List<Long> inputIds = List.of(-1L, -2L, -3L);
        List<Long> expectedIds = List.of();
        List<Long> existingIds = List.of();

        when(productRepository.findProductIdsByIdIn(expectedIds)).thenReturn(existingIds);

        // When
        List<Long> result = productService.productsExist(inputIds);

        // Then
        assertEquals(existingIds, result);
        verify(productRepository).findProductIdsByIdIn(expectedIds);
    }

    @Test
    void productsExist_WhenRepositoryThrowsException_PropagatesException() {
        // Given
        List<Long> inputIds = List.of(100L, 200L);
        RuntimeException expectedException = new RuntimeException("Database error");

        when(productRepository.findProductIdsByIdIn(inputIds)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.productsExist(inputIds));

        assertEquals(expectedException, exception);
        verify(productRepository).findProductIdsByIdIn(inputIds);
    }

    @Test
    void deleteProductImage_ProductExistsAndPreviewImage_RemovesPreviewImage() {
        // Given
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setPreviewImageFileName("old-preview.jpg");

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        // When
        productService.deleteProductImage(PRODUCT_ID, "old-preview.jpg", true);

        // Then
        assertNull(product.getPreviewImageFileName());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProductImage_ProductExistsAndNotPreviewImage_RemovesFromImageList() {
        // Given
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setImageFileNames(new ArrayList<>(List.of("image1.jpg", "image2.jpg", "image3.jpg")));

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        // When
        productService.deleteProductImage(PRODUCT_ID, "image2.jpg", false);

        // Then
        assertEquals(2, product.getImageFileNames().size());
        assertFalse(product.getImageFileNames().contains("image2.jpg"));
        verify(productRepository).save(product);
    }

    @Test
    void deleteProductImage_ProductNotExists_ThrowsProductNotFoundException() {
        // Given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // When & Then
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProductImage(PRODUCT_ID, "image.jpg", true));

        assertEquals(List.of(PRODUCT_ID), exception.getProductIds());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void setProductImage_ProductExistsAndPreviewImage_SetsPreviewImage() {
        // Given
        Product product = new Product();
        product.setId(PRODUCT_ID);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        // When
        productService.setProductImage(PRODUCT_ID, "new-preview.jpg", true);

        // Then
        assertEquals("new-preview.jpg", product.getPreviewImageFileName());
        verify(productRepository).save(product);
    }

    @Test
    void setProductImage_ProductExistsAndNotPreviewImage_AddsToImageList() {
        // Given
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setImageFileNames(new java.util.ArrayList<>(List.of("image1.jpg")));

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        // When
        productService.setProductImage(PRODUCT_ID, "image2.jpg", false);

        // Then
        assertEquals(2, product.getImageFileNames().size());
        assertTrue(product.getImageFileNames().contains("image2.jpg"));
        verify(productRepository).save(product);
    }

    @Test
    void setProductImage_ProductNotExists_ThrowsProductNotFoundException() {
        // Given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // When & Then
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productService.setProductImage(PRODUCT_ID, "image.jpg", true));

        assertEquals(List.of(PRODUCT_ID), exception.getProductIds());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getTotalPrice_WithValidProducts_CalculatesTotal() {
        // Given
        List<ProductIdAndQuantityDto> dtos = List.of(
                new ProductIdAndQuantityDto(100L, 2),
                new ProductIdAndQuantityDto(200L, 3),
                new ProductIdAndQuantityDto(300L, 1) // This product won't be found
        );

        Product product1 = new Product();
        product1.setId(100L);
        product1.setPrice(50);

        Product product2 = new Product();
        product2.setId(200L);
        product2.setPrice(100);

        when(productRepository.findAllById(List.of(100L, 200L, 300L)))
                .thenReturn(List.of(product1, product2));

        // When
        int result = productService.getTotalPrice(dtos);

        // Then
        assertEquals(400, result); // (50 * 2) + (100 * 3) + (0 * 1) = 100 + 300 + 0 = 400
        verify(productRepository).findAllById(List.of(100L, 200L, 300L));
    }

    @Test
    void getTotalPrice_WithEmptyList_ReturnsZero() {
        // When
        int result = productService.getTotalPrice(List.of());

        // Then
        assertEquals(0, result);
        verify(productRepository, never()).findAllById(anyList());
    }

    @Test
    void getTotalPrice_WhenNoProductsFound_ReturnsZero() {
        // Given
        List<ProductIdAndQuantityDto> dtos = List.of(
                new ProductIdAndQuantityDto(100L, 2)
        );

        when(productRepository.findAllById(List.of(100L))).thenReturn(List.of());

        // When
        int result = productService.getTotalPrice(dtos);

        // Then
        assertEquals(0, result);
        verify(productRepository).findAllById(List.of(100L));
    }

    @Test
    void getProductsPrice_WithValidIds_ReturnsPriceDtos() {
        // Given
        List<Long> productIds = List.of(100L, 200L);
        List<ProductIdAndPriceDto> expectedDtos = List.of(
                new ProductIdAndPriceDto(100L, 50),
                new ProductIdAndPriceDto(200L, 100)
        );

        when(productRepository.findIdAndPriceByIds(productIds)).thenReturn(expectedDtos);

        // When
        List<ProductIdAndPriceDto> result = productService.getProductsPrice(productIds);

        // Then
        assertEquals(expectedDtos, result);
        verify(productRepository).findIdAndPriceByIds(productIds);
    }

    @Test
    void getProductsPrice_WithEmptyList_ReturnsEmptyList() {
        // Given
        when(productRepository.findIdAndPriceByIds(List.of())).thenReturn(List.of());

        // When
        List<ProductIdAndPriceDto> result = productService.getProductsPrice(List.of());

        // Then
        assertTrue(result.isEmpty());
        verify(productRepository).findIdAndPriceByIds(List.of());
    }
}