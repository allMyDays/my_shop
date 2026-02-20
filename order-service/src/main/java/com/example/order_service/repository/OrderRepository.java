package com.example.order_service.repository;

import com.example.order_service.entity.Order;
import com.example.order_service.enumeration.OrderLivingStatus;
import com.example.order_service.enumeration.OrderSortingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.dateOfCreation DESC")
    List<Order> findAllByUserSortedByDateDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderStatus NOT IN :excludedStatuses ORDER BY o.dateOfCreation DESC")
    List<Order> findAllByUserWithExcludedStatuses(
        @Param("userId") Long userId,
        @Param("excludedStatuses") List<OrderLivingStatus> excludedStatuses,
        Pageable pageable

    );

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderStatus = :status ORDER BY o.dateOfCreation DESC")
    List<Order> findByOrderStatusAndUserSorted(
        @Param("userId") Long userId,
        @Param("status") OrderLivingStatus status,
        Pageable pageable

    );











}
