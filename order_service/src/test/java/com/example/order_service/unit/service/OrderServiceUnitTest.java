package com.example.order_service.unit.service;


import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.client.kafka.CartKafkaClient;
import com.example.common.dto.product.ProductIdAndPriceDto;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.ProductNotFoundException;
import com.example.common.exception.UserNotOwnerException;
import com.example.order_service.entity.DeliveryInfo;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.enumeration.OrderLivingStatus;
import com.example.order_service.enumeration.OrderSortingStatus;
import com.example.order_service.exception.AddressNotFoundException;
import com.example.order_service.exception.OrderAlreadyCancelledException;
import com.example.order_service.exception.OrderTooOldToCancelException;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.repository.OrderItemRepository;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.DeliveryInfoService;
import com.example.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductGrpcClient productGrpcClient;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private CartKafkaClient cartKafkaClient;

    @Mock
    private DeliveryInfoService deliveryInfoService;

    @InjectMocks
    private OrderService orderService;

    private final Long USER_ID = 1L;
    private final Long ORDER_ID = 100L;
    private final Long PRODUCT_ID_1 = 200L;
    private final Long PRODUCT_ID_2 = 300L;

    @Test
    void createOrder_WithValidData_CreatesOrderSuccessfully() {
        // Given
        List<ProductIdAndQuantityDto> itemsDtoList = List.of(
                new ProductIdAndQuantityDto(PRODUCT_ID_1, 2),
                new ProductIdAndQuantityDto(PRODUCT_ID_2, 3)
        );

        DeliveryInfo deliveryInfo = new DeliveryInfo();
        deliveryInfo.setUserAddress("Test Address");
        deliveryInfo.setDeliveryDistance(10_000);
        deliveryInfo.setDeliveryTime(3_000);
        when(deliveryInfoService.getDeliveryInfo(USER_ID)).thenReturn(Optional.of(deliveryInfo));

        List<ProductIdAndPriceDto> priceDtos = List.of(
                new ProductIdAndPriceDto(PRODUCT_ID_1, 100),
                new ProductIdAndPriceDto(PRODUCT_ID_2, 200)
        );
        when(productGrpcClient.getProductsPrice(anyList())).thenReturn(priceDtos);

        Order savedOrder = new Order();
        savedOrder.setId(ORDER_ID);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        orderService.createOrder(USER_ID, itemsDtoList);

        // Then
        verify(deliveryInfoService).getDeliveryInfo(USER_ID);
        verify(productGrpcClient).getProductsPrice(List.of(PRODUCT_ID_1, PRODUCT_ID_2));
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(cartKafkaClient).deleteCartItems(USER_ID, List.of(PRODUCT_ID_1, PRODUCT_ID_2));
    }

    @Test
    void createOrder_WhenDeliveryInfoNotFound_ThrowsAddressNotFoundException() {
        // Given
        List<ProductIdAndQuantityDto> itemsDtoList = List.of(
                new ProductIdAndQuantityDto(PRODUCT_ID_1, 2)
        );
        when(deliveryInfoService.getDeliveryInfo(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AddressNotFoundException.class,
                () -> orderService.createOrder(USER_ID, itemsDtoList));

        verify(productGrpcClient, never()).getProductsPrice(anyList());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WhenSomeProductsNotFound_ThrowsProductNotFoundException() {
        // Given
        List<ProductIdAndQuantityDto> itemsDtoList = List.of(
                new ProductIdAndQuantityDto(PRODUCT_ID_1, 2),
                new ProductIdAndQuantityDto(PRODUCT_ID_2, 3)
        );

        DeliveryInfo deliveryInfo = new DeliveryInfo();
        deliveryInfo.setUserAddress("Test Address");
        when(deliveryInfoService.getDeliveryInfo(USER_ID)).thenReturn(Optional.of(deliveryInfo));

        List<ProductIdAndPriceDto> priceDtos = List.of(
                new ProductIdAndPriceDto(PRODUCT_ID_1, 100)
                // PRODUCT_ID_2 is missing
        );
        when(productGrpcClient.getProductsPrice(anyList())).thenReturn(priceDtos);


// When & Then
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> orderService.createOrder(USER_ID, itemsDtoList));

        assertEquals(List.of(PRODUCT_ID_2), exception.getProductIds());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithDuplicateProducts_SumsQuantities() {
        // Given
        List<ProductIdAndQuantityDto> itemsDtoList = List.of(
                new ProductIdAndQuantityDto(PRODUCT_ID_1, 2),
                new ProductIdAndQuantityDto(PRODUCT_ID_1, 3) // Duplicate product
        );

        DeliveryInfo deliveryInfo = new DeliveryInfo();
        deliveryInfo.setUserAddress("Test Address");
        deliveryInfo.setDeliveryDistance(10_000);
        deliveryInfo.setDeliveryTime(3_000);
        when(deliveryInfoService.getDeliveryInfo(USER_ID)).thenReturn(Optional.of(deliveryInfo));

        List<ProductIdAndPriceDto> priceDtos = List.of(
                new ProductIdAndPriceDto(PRODUCT_ID_1, 100)
        );
        when(productGrpcClient.getProductsPrice(anyList())).thenReturn(priceDtos);

        Order savedOrder = new Order();
        savedOrder.setId(ORDER_ID);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        orderService.createOrder(USER_ID, itemsDtoList);

        // Then - Should create order with quantity 5 for PRODUCT_ID_1
        verify(orderItemRepository).save(argThat(item ->
                item.getProductId().equals(PRODUCT_ID_1) && item.getProductQuantity() == 5
        ));
    }

    @Test
    void cancelOrder_WithValidOrderAndReturnItems_CancelsAndReturnsItems() {
        // Given
        Order order = new Order();
        order.setUserId(USER_ID);
        order.setOrderStatus(OrderLivingStatus.IN_ASSEMBLING);
        order.setDateOfCreation(LocalDateTime.now().minusMinutes(30));

        List<OrderItem> orderItems = List.of(new OrderItem(), new OrderItem());
        order.setOrderItems(orderItems);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        List<ProductIdAndQuantityDto> dtos = List.of(
                new ProductIdAndQuantityDto(PRODUCT_ID_1, 2)
        );
        when(orderItemMapper.toProductIdAndQuantityDTOs(orderItems)).thenReturn(dtos);

        // When
        orderService.cancelOrder(USER_ID, ORDER_ID, true);

        // Then
        assertEquals(OrderLivingStatus.CANCELLED, order.getOrderStatus());
        verify(orderRepository).save(order);
        verify(cartKafkaClient).addItemsToCart(USER_ID, dtos);
    }

    @Test
    void cancelOrder_WithValidOrderWithoutReturnItems_CancelsWithoutReturningItems() {
        // Given
        Order order = new Order();
        order.setUserId(USER_ID);
        order.setOrderStatus(OrderLivingStatus.IN_ASSEMBLING);
        order.setDateOfCreation(LocalDateTime.now().minusMinutes(30));

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        // When
        orderService.cancelOrder(USER_ID, ORDER_ID, false);

        // Then
        assertEquals(OrderLivingStatus.CANCELLED, order.getOrderStatus());
        verify(orderRepository).save(order);
        verify(cartKafkaClient, never()).addItemsToCart(anyLong(), anyList());
    }

    @Test
    void cancelOrder_WhenOrderAlreadyCancelled_ThrowsOrderAlreadyCancelledException() {
        // Given
        Order order = new Order();
        order.setUserId(USER_ID);
        order.setOrderStatus(OrderLivingStatus.CANCELLED);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(OrderAlreadyCancelledException.class,
                () -> orderService.cancelOrder(USER_ID, ORDER_ID, true));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenOrderTooOld_ThrowsOrderTooOldToCancelException() {
        // Given
        Order order = new Order();
        order.setUserId(USER_ID);
        order.setOrderStatus(OrderLivingStatus.IN_ASSEMBLING);
        order.setDateOfCreation(LocalDateTime.now().minusHours(2)); // More than 1 hour

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(OrderTooOldToCancelException.class,
                () -> orderService.cancelOrder(USER_ID, ORDER_ID, true));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        Order order = new Order();
        order.setUserId(999L); // Different user

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> orderService.cancelOrder(USER_ID, ORDER_ID, true));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenOrderNotFound_ThrowsEntityNotFoundException() {
        // Given
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> orderService.cancelOrder(USER_ID, ORDER_ID, true));

        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void getOrders_WithAllStatus_ReturnsAllOrders() {
        // Given
        int offset = 0;

        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        List<Order> expectedOrders = List.of(order1, order2);

        when(orderRepository.findAllByUserSortedByDateDesc(eq(USER_ID), any(Pageable.class)))
                .thenReturn(expectedOrders);

        // Используем lenient() чтобы избежать конфликта стабов
        lenient().when(orderItemRepository.findByOrderIdWithPagination(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(new OrderItem()));
        lenient().when(orderItemRepository.findByOrderIdWithPagination(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(new OrderItem()));

        // When
        List<Order> result = orderService.getOrders(OrderSortingStatus.ALL, USER_ID, offset);

        // Then
        assertEquals(2, result.size());
        verify(orderRepository).findAllByUserSortedByDateDesc(eq(USER_ID),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 40));
    }

    @Test
    void getOrders_WithActiveStatus_ReturnsActiveOrders() {
        // Given
        int offset = 0;

        Order order = new Order();
        order.setId(1L);
        List<Order> expectedOrders = List.of(order);

        when(orderRepository.findAllByUserWithExcludedStatuses(eq(USER_ID), anyList(), any(Pageable.class)))
                .thenReturn(expectedOrders);

        lenient().when(orderItemRepository.findByOrderIdWithPagination(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(new OrderItem()));

        // When
        List<Order> result = orderService.getOrders(OrderSortingStatus.ACTIVE, USER_ID, offset);

        // Then
        assertEquals(1, result.size());
        verify(orderRepository).findAllByUserWithExcludedStatuses(eq(USER_ID),
                eq(List.of(OrderLivingStatus.DELIVERED, OrderLivingStatus.CANCELLED)), any(Pageable.class));
    }

    @Test
    void getOrders_WithCompletedStatus_ReturnsCompletedOrders() {
        // Given
        int offset = 0;

        Order order = new Order();
        order.setId(1L);
        List<Order> expectedOrders = List.of(order);

        when(orderRepository.findByOrderStatusAndUserSorted(eq(USER_ID), eq(OrderLivingStatus.DELIVERED), any(Pageable.class)))
                .thenReturn(expectedOrders);

        lenient().when(orderItemRepository.findByOrderIdWithPagination(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(new OrderItem()));

        // When
        List<Order> result = orderService.getOrders(OrderSortingStatus.COMPLETED, USER_ID, offset);

        // Then


        assertEquals(1, result.size());
        verify(orderRepository).findByOrderStatusAndUserSorted(eq(USER_ID),
                eq(OrderLivingStatus.DELIVERED), any(Pageable.class));
    }

    @Test
    void getOrders_WithCancelledStatus_ReturnsCancelledOrders() {
        // Given
        int offset = 0;

        Order order = new Order();
        order.setId(1L);
        List<Order> expectedOrders = List.of(order);

        when(orderRepository.findByOrderStatusAndUserSorted(eq(USER_ID), eq(OrderLivingStatus.CANCELLED), any(Pageable.class)))
                .thenReturn(expectedOrders);

        lenient().when(orderItemRepository.findByOrderIdWithPagination(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(new OrderItem()));

        // When
        List<Order> result = orderService.getOrders(OrderSortingStatus.CANCELLED, USER_ID, offset);

        // Then
        assertEquals(1, result.size());
        verify(orderRepository).findByOrderStatusAndUserSorted(eq(USER_ID),
                eq(OrderLivingStatus.CANCELLED), any(Pageable.class));
    }

    @Test
    void getOrders_LimitsOrderItemsTo5() {
        // Given
        int offset = 0;

        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        List<Order> orders = List.of(order1, order2);

        when(orderRepository.findAllByUserSortedByDateDesc(eq(USER_ID), any(Pageable.class)))
                .thenReturn(orders);

        // Использую конкретные значения для проверки
        when(orderItemRepository.findByOrderIdWithPagination(eq(1L),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 5)))
                .thenReturn(List.of(new OrderItem()));
        when(orderItemRepository.findByOrderIdWithPagination(eq(2L),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 5)))
                .thenReturn(List.of(new OrderItem()));

        // When
        List<Order> result = orderService.getOrders(OrderSortingStatus.ALL, USER_ID, offset);

        // Then
        verify(orderItemRepository).findByOrderIdWithPagination(eq(1L),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 5));
        verify(orderItemRepository).findByOrderIdWithPagination(eq(2L),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 5));
    }

    @Test
    void getOrderItems_WithValidOrder_ReturnsItems() {
        // Given
        int offset = 0;
        Order order = new Order();
        order.setUserId(USER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        List<OrderItem> expectedItems = List.of(new OrderItem(), new OrderItem());
        when(orderItemRepository.findByOrderIdWithPagination(eq(ORDER_ID), any(Pageable.class)))
                .thenReturn(expectedItems);

        // When
        List<OrderItem> result = orderService.getOrderItems(ORDER_ID, USER_ID, offset);

        // Then
        assertEquals(2, result.size());
        verify(orderItemRepository).findByOrderIdWithPagination(eq(ORDER_ID),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 40));
    }

    // Альтернативное решение - использовать более гибкие матчеры
    @Test
    void getOrders_WithFlexibleMatching_ReturnsAllOrders() {
        // Given
        int offset = 0;

        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        List<Order> expectedOrders = List.of(order1, order2);

        when(orderRepository.findAllByUserSortedByDateDesc(eq(USER_ID), any(Pageable.class)))
                .thenReturn(expectedOrders);

        // Используем any() для обоих параметров
        when(orderItemRepository.findByOrderIdWithPagination(any(Long.class), any(Pageable.class)))
                .thenReturn(List.of(new OrderItem()));


// When
        List<Order> result = orderService.getOrders(OrderSortingStatus.ALL, USER_ID, offset);

        // Then
        assertEquals(2, result.size());
        // Проверяем, что метод вызывался 2 раза с любыми Long и Pageable
        verify(orderItemRepository, times(2)).findByOrderIdWithPagination(any(Long.class), any(Pageable.class));
    }


    @Test
    void getOrders_WithNegativeOffset_ThrowsIllegalArgumentException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> orderService.getOrders(OrderSortingStatus.ALL, USER_ID, -1));

        verify(orderRepository, never()).findAllByUserSortedByDateDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getOrderItems_WithNegativeOffset_ThrowsIllegalArgumentException() {
        // Given
        Order order = new Order();
        order.setUserId(USER_ID);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> orderService.getOrderItems(ORDER_ID, USER_ID, -1));

        verify(orderItemRepository, never()).findByOrderIdWithPagination(anyLong(), any(Pageable.class));
    }

    @Test
    void getOrderItems_WithUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        Order order = new Order();
        order.setUserId(999L); // Different user
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> orderService.getOrderItems(ORDER_ID, USER_ID, 0));

        verify(orderItemRepository, never()).findByOrderIdWithPagination(anyLong(), any(Pageable.class));
    }

    @Test
    void getProductIdsByOrderId_ReturnsProductIds() {
        // Given
        List<Long> expectedIds = List.of(PRODUCT_ID_1, PRODUCT_ID_2);
        when(orderItemRepository.findProductIdsByOrderId(ORDER_ID)).thenReturn(expectedIds);

        // When
        List<Long> result = orderService.getProductIdsByOrderId(ORDER_ID);

        // Then
        assertEquals(expectedIds, result);
        verify(orderItemRepository).findProductIdsByOrderId(ORDER_ID);
    }

    @Test
    void validateEntityAndOwnership_WithValidOwnership_ReturnsOrder() {
        // Given
        Order order = new Order();
        order.setUserId(USER_ID);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // When
        Order result = orderService.validateEntityAndOwnership(USER_ID, ORDER_ID);

        // Then
        assertEquals(order, result);
    }

    @Test
    void validateEntityAndOwnership_WhenOrderNotFound_ThrowsEntityNotFoundException() {
        // Given
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> orderService.validateEntityAndOwnership(USER_ID, ORDER_ID));
    }

    @Test
    void validateEntityAndOwnership_WhenUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        Order order = new Order();
        order.setUserId(999L); // Different user
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> orderService.validateEntityAndOwnership(USER_ID, ORDER_ID));
    }
}