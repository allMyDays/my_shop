package com.example.order_service.service;

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
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.order_service.enumeration.OrderLivingStatus.*;
import static com.example.order_service.service.DeliveryInfoService.calculateDeliveryPrice;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductGrpcClient productGrpcClient;

    private final OrderItemRepository orderItemRepository;

    private final OrderItemMapper orderItemMapper;

    private final CartKafkaClient cartKafkaClient;

    private final DeliveryInfoService deliveryInfoService;


    public void createOrder(long userId, @NonNull List<ProductIdAndQuantityDto> itemsDtoList) {

        Map<Long, Integer> productMapQuantity = itemsDtoList.stream()
                .collect(Collectors.toMap(ProductIdAndQuantityDto::getProductId, ProductIdAndQuantityDto::getProductQuantity, Integer::sum));

        List<Long> allProductIds = new ArrayList<>(productMapQuantity.keySet().stream().toList());

        Optional<DeliveryInfo> deliveryInfoOptional = deliveryInfoService.getDeliveryInfo(userId);
        if(deliveryInfoOptional.isEmpty()){
            throw new AddressNotFoundException(userId);
        }
        DeliveryInfo deliveryInfo = deliveryInfoOptional.get();

        List<ProductIdAndPriceDto> idAndPriceDTOs =  productGrpcClient.getProductsPrice(allProductIds);

        Map<Long, Integer> productMapPrice = idAndPriceDTOs.stream()
                .collect(Collectors.toMap(ProductIdAndPriceDto::getProductId, ProductIdAndPriceDto::getProductPrice));

        if(idAndPriceDTOs.size()<allProductIds.size()){
            allProductIds.removeAll(productMapPrice.keySet());
            throw new ProductNotFoundException(allProductIds);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setDateOfCreation(LocalDateTime.now());
        order.setOrderStatus(IN_ASSEMBLING);
        order.setAddress(deliveryInfo.getUserAddress());
        order.setUniqueItemQuantity(productMapQuantity.size());
        order.setCommonItemQuantity(
                productMapQuantity.values().stream()
                        .mapToInt(a->a)
                        .sum());
        order.setDeliveryPrice(calculateDeliveryPrice(deliveryInfo.getDeliveryDistance(), deliveryInfo.getDeliveryTime()));
        order.setTotalPrice(
                productMapPrice.keySet().stream()
                        .mapToInt(a->productMapQuantity.get(a)*productMapPrice.get(a))
                        .sum()
        );
        order = orderRepository.save(order);

        for(int i=0; i<allProductIds.size(); i++){
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(allProductIds.get(i));
            orderItem.setOrder(order);
            orderItem.setPosition(i);
            orderItem.setProductQuantity(productMapQuantity.get(orderItem.getProductId()));
            orderItem.setProductPrice(productMapPrice.get(orderItem.getProductId()));
            orderItemRepository.save(orderItem);
        }

        cartKafkaClient.deleteCartItems(userId, allProductIds);


    }
    @Transactional
    public void cancelOrder(long userId, long orderId, boolean returnItemsToCart) {

       Order order = validateEntityAndOwnership(userId, orderId);

       if(order.getOrderStatus().equals(CANCELLED)){
           throw new OrderAlreadyCancelledException();
       }
        if(ChronoUnit.HOURS.between(order.getDateOfCreation(),LocalDateTime.now())>=1){
            throw new OrderTooOldToCancelException();
        }
        order.setOrderStatus(CANCELLED);
        orderRepository.save(order);

        if(returnItemsToCart){
            cartKafkaClient.addItemsToCart(userId, orderItemMapper.toProductIdAndQuantityDTOs(order.getOrderItems()));

        }

    }

    public List<Order> getOrders(@NonNull OrderSortingStatus sortingStatus, long userId, int offset) {

        if(offset<0) throw new IllegalArgumentException("offset must be greater or equal to 0");

        int limit = 40;

        Pageable pageable = PageRequest.of(offset/limit, limit);

        List<Order> orders =  switch (sortingStatus) {
            case ALL -> orderRepository.findAllByUserSortedByDateDesc(userId,pageable);
            case ACTIVE -> orderRepository.findAllByUserWithExcludedStatuses(userId, List.of(DELIVERED,CANCELLED),pageable);
            case COMPLETED -> orderRepository.findByOrderStatusAndUserSorted(userId, DELIVERED,pageable);
            case CANCELLED -> orderRepository.findByOrderStatusAndUserSorted(userId, OrderLivingStatus.CANCELLED,pageable);
        };

        int itemLimit = 5;

        orders.forEach(o-> o.setOrderItems(orderItemRepository.findByOrderIdWithPagination(o.getId(), PageRequest.of(0, itemLimit))));

        return orders;
    }

    public List<OrderItem> getOrderItems(long orderId, long userId, int offset) {

        validateEntityAndOwnership(userId, orderId);

        if(offset<0) throw new IllegalArgumentException("offset must be greater or equal to 0");

        int limit = 40;

        return orderItemRepository.findByOrderIdWithPagination(orderId, PageRequest.of(offset/limit, limit));

    }

    public List<Long> getProductIdsByOrderId(long orderId){
        return orderItemRepository.findProductIdsByOrderId(orderId);
    }



    public Order validateEntityAndOwnership(long userId, long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(Order.class,orderId));

        if(!order.getUserId().equals(userId)){
            throw new UserNotOwnerException(userId,orderId,Order.class);
        }
        return order;
    }

}
