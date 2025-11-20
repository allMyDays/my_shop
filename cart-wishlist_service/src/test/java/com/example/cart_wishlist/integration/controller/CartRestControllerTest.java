package com.example.cart_wishlist.integration.controller;

import com.example.cart_wishlist.controller.rest.CartRestController;
import com.example.cart_wishlist.exception.TooManyItemsException;
import com.example.cart_wishlist.mapper.CartItemMapper;
import com.example.cart_wishlist.mapper.CartMapper;
import com.example.cart_wishlist.service.CartService;
import com.example.common.dto.cart.rest.CartItemResponseDTO;
import com.example.common.dto.cart.rest.CartResponseDTO;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartRestController.class)
class CartRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private CartItemMapper cartItemMapper;

    @MockBean
    private CartMapper cartMapper;

    private final Long TEST_USER_ID = 1L;
    private final Long TEST_PRODUCT_ID = 100L;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor getJwt() {
        return jwt().jwt(jwt -> jwt.claim(MY_USER_ID_KEY_KEYCLOAK, 1));
    }

    @Test
    void getCart_AuthenticatedUser_ReturnsCart() throws Exception {
        CartResponseDTO expectedCart = new CartResponseDTO();
        when(cartMapper.toCartResponseDTO(TEST_USER_ID)).thenReturn(expectedCart);

        mockMvc.perform(get("/api/cart")
                        .with(getJwt()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedCart)));

        verify(cartMapper).toCartResponseDTO(TEST_USER_ID);
    }

    @Test
    void getCart_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateItemQuantityByOne_IncreaseQuantity_ReturnsNewQuantity() throws Exception {
        int newQuantity = 5;
        when(cartService.updateProductQuantity(TEST_USER_ID, TEST_PRODUCT_ID, true))
                .thenReturn(newQuantity);

        mockMvc.perform(put("/api/cart/{productId}", TEST_PRODUCT_ID)
                        .param("increase", "true")
                        .param("pricePerProduct", "100")
                        .with(getJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newQuantity").value(newQuantity))
                .andExpect(jsonPath("$.totalPriceView").value("500₽"));

        verify(cartService).updateProductQuantity(TEST_USER_ID, TEST_PRODUCT_ID, true);
    }

    @Test
    void updateItemQuantityByOne_DecreaseQuantity_ReturnsNewQuantity() throws Exception {
        int newQuantity = 3;
        when(cartService.updateProductQuantity(TEST_USER_ID, TEST_PRODUCT_ID, false))
                .thenReturn(newQuantity);

        mockMvc.perform(put("/api/cart/{productId}", TEST_PRODUCT_ID)
                        .param("increase", "false")
                        .with(getJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newQuantity").value(newQuantity))
                .andExpect(jsonPath("$.totalPriceView").doesNotExist());

        verify(cartService).updateProductQuantity(TEST_USER_ID, TEST_PRODUCT_ID, false);
    }

    @Test
    void updateItemQuantityByOne_InvalidProductId_ReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/cart/invalidId")
                        .param("increase", "true")
                        .with(getJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCartItems_WithOffset_ReturnsItems() throws Exception {
        int offset = 10;
        List<CartItemResponseDTO> expectedItems = List.of(new CartItemResponseDTO(), new CartItemResponseDTO());
        when(cartItemMapper.mapCartItems(cartService.getCartItems(TEST_USER_ID, offset)))
                .thenReturn(expectedItems);

        mockMvc.perform(get("/api/cart/items")
                        .param("offset", String.valueOf(offset))
                        .with(getJwt()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedItems)));

        verify(cartItemMapper).mapCartItems(cartService.getCartItems(TEST_USER_ID, offset));
    }

    @Test
    void getCartSize_AuthenticatedUser_ReturnsSize() throws Exception {
        int expectedSize = 15;
        when(cartService.getCartSize(TEST_USER_ID)).thenReturn(expectedSize);

        mockMvc.perform(get("/api/cart/size")
                        .with(getJwt()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedSize)));

        verify(cartService).getCartSize(TEST_USER_ID);
    }

    @Test
    void addToCart_ValidRequest_ReturnsOk() throws Exception {
        mockMvc.perform(post("/api/cart/add")
                        .param("productId", TEST_PRODUCT_ID.toString())
                        .param("quantity", "2")
                        .with(getJwt()))
                .andExpect(status().isOk());

        verify(cartService).addItemToCart(TEST_USER_ID, TEST_PRODUCT_ID, 2);
    }

    @Test
    void addToCart_TooManyItems_ReturnsTooManyRequests() throws Exception {
        String errorMessage = "Вы превысили лимит на добавление товаров в корзину. Удалите часть товаров, чтобы освободить место.";
        doThrow(new TooManyItemsException(true))
                .when(cartService).addItemToCart(TEST_USER_ID, TEST_PRODUCT_ID, 2);

        mockMvc.perform(post("/api/cart/add")
                        .param("productId", TEST_PRODUCT_ID.toString())
                        .param("quantity", "2")
                        .with(getJwt()))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string(errorMessage));

        verify(cartService).addItemToCart(TEST_USER_ID, TEST_PRODUCT_ID, 2);
    }

    @Test
    void removeFromCart_ValidProduct_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/cart/{productId}", TEST_PRODUCT_ID)
                        .with(getJwt()))
                .andExpect(status().isOk());

        verify(cartService).removeItemFromCart(TEST_USER_ID, TEST_PRODUCT_ID);
    }

    @Test
    void getProductIDs_AuthenticatedUser_ReturnsProductIds() throws Exception {
        List<Long> expectedIds = List.of(1L, 2L, 3L);
        when(cartService.getProductIdsByUserId(TEST_USER_ID)).thenReturn(expectedIds);

        mockMvc.perform(get("/api/cart/get_product_ids")
                        .with(getJwt()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedIds)));

        verify(cartService).getProductIdsByUserId(TEST_USER_ID);
    }

    @Test
    void isProductInCart_ProductExists_ReturnsTrue() throws Exception {
        when(cartService.productExists(TEST_PRODUCT_ID, TEST_USER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/cart/product-exists")
                        .param("productId", TEST_PRODUCT_ID.toString())
                        .with(getJwt()))
                .andExpect(status().isOk())


                .andExpect(content().string("true"));

        verify(cartService).productExists(TEST_PRODUCT_ID, TEST_USER_ID);
    }

    @Test
    void getBriefItems_AuthenticatedUser_ReturnsBriefItems() throws Exception {
        List<ProductIdAndQuantityDto> expectedItems = List.of(
                new ProductIdAndQuantityDto(1L, 2),
                new ProductIdAndQuantityDto(2L, 1)
        );
        when(cartService.getBriefItems(TEST_USER_ID)).thenReturn(expectedItems);

        mockMvc.perform(get("/api/cart/brief-items")
                        .with(getJwt()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedItems)));

        verify(cartService).getBriefItems(TEST_USER_ID);
    }

}
