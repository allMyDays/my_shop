package com.example.review_service.repository;

import com.example.review_service.dto.ProductReviewStats;
import com.example.review_service.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId, Pageable pageable);

    @Query("SELECT r.productId AS productId, " +
            "COUNT(r) AS reviewCount, " +
            "AVG(r.rating) AS averageRating " +
            "FROM Review r " +
            "WHERE r.productId IN :productIds " +
            "GROUP BY r.productId")
    List<ProductReviewStats> findReviewStatsByProductIds(@Param("productIds") List<Long> productIds);

    //  от большего к меньшему
    List<Review> findByProductIdOrderByRatingDesc(Long productId, Pageable pageable);

    // от меньшего к большему
    List<Review> findByProductIdOrderByRatingAsc(Long productId, Pageable pageable);

    // от новых к старым
    List<Review> findByProductIdOrderByDateOfCreationDesc(Long productId, Pageable pageable);

    //  по наличию фото
   @Query("SELECT r FROM Review r WHERE r.productId = :productId ORDER BY SIZE(r.photoFileNames) DESC")
    List<Review> findByProductIdOrderByPhotoPresence(@Param("productId") Long productId, Pageable pageable);

    // отзыв по userId
    Optional<Review> findOneByUserIdAndProductId(Long userId, Long productId);

    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);



}