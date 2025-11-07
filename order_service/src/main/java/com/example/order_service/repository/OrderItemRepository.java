package com.example.order_service.repository;

import com.example.order_service.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

       @Query("SELECT pi FROM OrderItem pi WHERE pi.order.id = :orderId ORDER BY pi.position ASC")
       List<OrderItem> findByOrderIdWithPagination(@Param("orderId") Long orderId, Pageable pageable);

       @Query("SELECT oi.productId FROM OrderItem oi WHERE oi.order.id = :orderId")
       List<Long> findProductIdsByOrderId(@Param("orderId") Long orderId);

}
