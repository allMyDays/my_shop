package com.example.cart_wishlist.repository;

import com.example.cart_wishlist.entity.WishItem;
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
public interface WishItemRepository extends JpaRepository<WishItem, Long> {

     @Query("SELECT wi FROM WishItem wi WHERE wi.wishList.userId = :userId ORDER BY wi.id")
     List<WishItem> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

     @Query("SELECT COUNT(wi) FROM WishItem wi WHERE wi.wishList.userId = :userId")
    long countQuantityByUserId(@Param("userId") Long userId);


     Optional<WishItem> findByWishListUserIdAndProductId(Long userId, Long productId);



    @Modifying
    @Transactional
    @Query("DELETE FROM WishItem wi WHERE wi.wishList.userId = :userId AND wi.productId = :productId")
        void deleteByUserIdAndProductId(@Param("userId") Long userId,
                                    @Param("productId") Long productId);



    @Query("SELECT wi.productId FROM WishItem wi WHERE wi.wishList.userId = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);

    boolean existsByWishListUserIdAndProductId(Long userId, Long productId);




    }





