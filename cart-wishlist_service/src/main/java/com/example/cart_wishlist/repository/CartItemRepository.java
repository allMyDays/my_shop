package com.example.cart_wishlist.repository;

import com.example.cart_wishlist.entity.CartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

     @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId ORDER BY ci.id")
     List<CartItem> findByUserId(@Param("userId") Long userId, Pageable pageable);

     @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.userId = :userId")
    long sumQuantityByUserId(@Param("userId") Long userId);

     Optional<CartItem> findByCartUserIdAndProductId(Long userId, Long productId);



    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.productId = :productId")
        void deleteByUserIdAndProductId(@Param("userId") Long userId,
                                    @Param("productId") Long productId);


    @Query("SELECT ci.productId FROM CartItem ci WHERE ci.cart.userId = :userId")
     List<Long> findProductIdsByUserId(@Param("userId") Long userId);

    boolean existsByCartUserIdAndProductId(Long userId, Long productId);



}



