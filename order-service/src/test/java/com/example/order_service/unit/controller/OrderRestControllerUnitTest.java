package com.example.order_service.unit.controller;

import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.UserNotFoundException;
import com.example.order_service.controller.rest.OrderRestController;
import com.example.order_service.dto.OrderItemResponseDto;
import com.example.order_service.dto.OrderResponseDto;
import com.example.order_service.entity.DeliveryInfo;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.enumeration.OrderSortingStatus;
import com.example.order_service.exception.AddressNotFoundException;
import com.example.order_service.exception.OrderAlreadyCancelledException;
import com.example.order_service.exception.OrderTooOldToCancelException;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.service.DeliveryInfoService;
import com.example.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRestControllerUnitTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private DeliveryInfoService deliveryInfoService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private OrderRestController orderRestController;

    private final Long TEST_USER_ID = 123L;
    private final Long TEST_ORDER_ID = 456L;

    @BeforeEach
    void setUp() {
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
    }

    // Tests for createOrder method
    @Test
    void createOrder_WhenValidRequest_ShouldReturnOk() {
        // Arrange
        List<ProductIdAndQuantityDto> items = List.of(
                new ProductIdAndQuantityDto(1L, 2),
                new ProductIdAndQuantityDto(2L, 1)
        );

        doNothing().when(orderService).createOrder(eq(TEST_USER_ID), eq(items));

        // Act
        ResponseEntity<?> response = orderRestController.createOrder(items, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).createOrder(TEST_USER_ID, items);
    }

    @Test
    void createOrder_WhenAddressNotFound_ShouldReturnConflict() {
        // Arrange
        List<ProductIdAndQuantityDto> items = List.of(new ProductIdAndQuantityDto(1L, 1));

        doThrow(new AddressNotFoundException(TEST_USER_ID))
                .when(orderService).createOrder(eq(TEST_USER_ID), eq(items));

        // Act
        ResponseEntity<?> response = orderRestController.createOrder(items, jwt);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("this function requires an user living address that was not found for user with id " + TEST_USER_ID, response.getBody());
        verify(orderService).createOrder(TEST_USER_ID, items);
    }

    @Test
    void createOrder_WhenEmptyItemsList_ShouldHandleGracefully() {
        // Arrange
        List<ProductIdAndQuantityDto> emptyItems = List.of();
        doNothing().when(orderService).createOrder(eq(TEST_USER_ID), eq(emptyItems));

        // Act
        ResponseEntity<?> response = orderRestController.createOrder(emptyItems, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).createOrder(TEST_USER_ID, emptyItems);
    }

    // Tests for checkCreationOrderAbility method
    @Test
    void checkCreationOrderAbility_WhenDeliveryInfoExists_ShouldReturnOk() {
        // Arrange
        DeliveryInfo deliveryInfo = new DeliveryInfo();
        when(deliveryInfoService.getDeliveryInfo(eq(TEST_USER_ID)))
                .thenReturn(Optional.of(deliveryInfo));

        // Act
        ResponseEntity<?> response = orderRestController.checkCreationOrderAbility(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryInfoService).getDeliveryInfo(TEST_USER_ID);
    }

    @Test
    void checkCreationOrderAbility_WhenNoDeliveryInfo_ShouldReturnConflict() {
        // Arrange
        when(deliveryInfoService.getDeliveryInfo(eq(TEST_USER_ID)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = orderRestController.checkCreationOrderAbility(jwt);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("this function requires an user living address that was not found for user with id " + TEST_USER_ID));
        verify(deliveryInfoService).getDeliveryInfo(TEST_USER_ID);
    }

    // Tests for cancelOrder method
    @Test
    void cancelOrder_WhenValidRequest_ShouldReturnOk() {
        // Arrange
        boolean returnItemsToCart = true;
        doNothing().when(orderService).cancelOrder(eq(TEST_USER_ID), eq(TEST_ORDER_ID), eq(returnItemsToCart));

        // Act
        ResponseEntity<?> response = orderRestController.cancelOrder(TEST_ORDER_ID, returnItemsToCart, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).cancelOrder(TEST_USER_ID, TEST_ORDER_ID, returnItemsToCart);
    }

    @Test
    void cancelOrder_WhenOrderAlreadyCancelled_ShouldReturnConflict() {
        // Arrange
        boolean returnItemsToCart = false;
        doThrow(new OrderAlreadyCancelledException())
                .when(orderService).cancelOrder(eq(TEST_USER_ID), eq(TEST_ORDER_ID), eq(returnItemsToCart));

        // Act
        ResponseEntity<?> response = orderRestController.cancelOrder(TEST_ORDER_ID, returnItemsToCart, jwt);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Данный заказ уже был отменен, повторная отмена не требуется.", response.getBody());
        verify(orderService).cancelOrder(TEST_USER_ID, TEST_ORDER_ID, returnItemsToCart);
    }

    @Test
    void cancelOrder_WhenOrderTooOldToCancel_ShouldReturnConflict() {
        // Arrange
        boolean returnItemsToCart = true;
        doThrow(new OrderTooOldToCancelException())
                .when(orderService).cancelOrder(eq(TEST_USER_ID), eq(TEST_ORDER_ID), eq(returnItemsToCart));

        // Act
        ResponseEntity<?> response = orderRestController.cancelOrder(TEST_ORDER_ID, returnItemsToCart, jwt);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("К сожалению, данный заказ не может быть отменен, так как он был создан более часа назад.", response.getBody());
        verify(orderService).cancelOrder(TEST_USER_ID, TEST_ORDER_ID, returnItemsToCart);
    }

    @Test
    void cancelOrder_WhenNotReturningItemsToCart_ShouldPassCorrectParameter() {
        // Arrange
        boolean returnItemsToCart = false;
        doNothing().when(orderService).cancelOrder(eq(TEST_USER_ID), eq(TEST_ORDER_ID), eq(returnItemsToCart));

        // Act
        ResponseEntity<?> response = orderRestController.cancelOrder(TEST_ORDER_ID, returnItemsToCart, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).cancelOrder(TEST_USER_ID, TEST_ORDER_ID, false);
    }

    // Tests for getOrders method
    @Test
    void getOrders_WhenValidRequest_ShouldReturnOrders() {
        // Arrange
        OrderSortingStatus sortingStatus = OrderSortingStatus.ACTIVE;
        int offset = 0;
        List<Order> orders = List.of(new Order(), new Order());
        List<OrderResponseDto> expectedDtos = List.of(new OrderResponseDto(), new OrderResponseDto());

        when(orderService.getOrders(eq(sortingStatus), eq(TEST_USER_ID), eq(offset)))
                .thenReturn(orders);
        when(orderMapper.toOrderResponseDTOs(eq(orders))).thenReturn(expectedDtos);

        // Act
        List<OrderResponseDto> result = orderRestController.getOrders(sortingStatus, offset, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(orderService).getOrders(sortingStatus, TEST_USER_ID, offset);
        verify(orderMapper).toOrderResponseDTOs(orders);
    }

    @Test
    void getOrders_WithDifferentSortingStatus_ShouldPassCorrectParameters() {
        // Arrange
        OrderSortingStatus sortingStatus = OrderSortingStatus.COMPLETED;
        int offset = 20;
        List<Order> orders = List.of(new Order());
        List<OrderResponseDto> expectedDtos = List.of(new OrderResponseDto());

        when(orderService.getOrders(eq(sortingStatus), eq(


                TEST_USER_ID), eq(offset)))
                .thenReturn(orders);
        when(orderMapper.toOrderResponseDTOs(eq(orders))).thenReturn(expectedDtos);

        // Act
        List<OrderResponseDto> result = orderRestController.getOrders(sortingStatus, offset, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(orderService).getOrders(OrderSortingStatus.COMPLETED, TEST_USER_ID, 20);
    }

    @Test
    void getOrders_WhenNoOrders_ShouldReturnEmptyList() {
        // Arrange
        OrderSortingStatus sortingStatus = OrderSortingStatus.ACTIVE;
        int offset = 0;
        List<Order> emptyOrders = List.of();
        List<OrderResponseDto> emptyDtos = List.of();

        when(orderService.getOrders(eq(sortingStatus), eq(TEST_USER_ID), eq(offset)))
                .thenReturn(emptyOrders);
        when(orderMapper.toOrderResponseDTOs(eq(emptyOrders))).thenReturn(emptyDtos);

        // Act
        List<OrderResponseDto> result = orderRestController.getOrders(sortingStatus, offset, jwt);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderService).getOrders(sortingStatus, TEST_USER_ID, offset);
    }

    // Tests for getOrderItems method
    @Test
    void getOrderItems_WhenValidRequest_ShouldReturnOrderItems() throws UserNotFoundException {
        // Arrange
        int offset = 0;
        List<OrderItem> orderItems = List.of(new OrderItem(), new OrderItem());
        List<OrderItemResponseDto> expectedDtos = List.of(new OrderItemResponseDto(), new OrderItemResponseDto());

        when(orderService.getOrderItems(eq(TEST_ORDER_ID), eq(TEST_USER_ID), eq(offset)))
                .thenReturn(orderItems);
        when(orderItemMapper.mapOrderItems(eq(orderItems))).thenReturn(expectedDtos);

        // Act
        List<OrderItemResponseDto> result = orderRestController.getOrderItems(TEST_ORDER_ID, offset, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(orderService).getOrderItems(TEST_ORDER_ID, TEST_USER_ID, offset);
        verify(orderItemMapper).mapOrderItems(orderItems);
    }

    @Test
    void getOrderItems_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        int offset = 0;
        when(orderService.getOrderItems(eq(TEST_ORDER_ID), eq(TEST_USER_ID), eq(offset)))
                .thenThrow(new UserNotFoundException());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            orderRestController.getOrderItems(TEST_ORDER_ID, offset, jwt);
        });

        verify(orderService).getOrderItems(TEST_ORDER_ID, TEST_USER_ID, offset);
        verify(orderItemMapper, never()).mapOrderItems(any());
    }

    @Test
    void getOrderItems_WithOffset_ShouldPassCorrectOffset() throws UserNotFoundException {
        // Arrange
        int offset = 40;
        List<OrderItem> orderItems = List.of(new OrderItem());
        List<OrderItemResponseDto> expectedDtos = List.of(new OrderItemResponseDto());

        when(orderService.getOrderItems(eq(TEST_ORDER_ID), eq(TEST_USER_ID), eq(offset)))
                .thenReturn(orderItems);
        when(orderItemMapper.mapOrderItems(eq(orderItems))).thenReturn(expectedDtos);

        // Act
        List<OrderItemResponseDto> result = orderRestController.getOrderItems(TEST_ORDER_ID, offset, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(orderService).getOrderItems(TEST_ORDER_ID, TEST_USER_ID, 40);
    }

}