package com.example.review_service.unit.service;

import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.client.kafka.MediaKafkaClient;
import com.example.common.exception.*;
import com.example.review_service.dto.ProductReviewInfoDto;
import com.example.review_service.dto.ProductReviewStats;
import com.example.review_service.entity.Review;
import com.example.review_service.enumeration.EditReviewAbilityStatus;
import com.example.review_service.enumeration.ReviewSortType;
import com.example.review_service.enumeration.UsagePeriod;
import com.example.review_service.exception.NoChangesInEditingReviewException;
import com.example.review_service.exception.ReviewAlreadyExistsException;
import com.example.review_service.repository.ReviewRepository;
import com.example.review_service.service.RedisService;
import com.example.review_service.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ProductGrpcClient productGrpcClient;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MediaKafkaClient mediaKafkaClient;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private ReviewService reviewService;

    private final Long USER_ID = 1L;
    private final Long PRODUCT_ID = 100L;
    private final Long REVIEW_ID = 200L;

    @Test
    void create_WithValidData_CreatesReviewSuccessfully() {
        // Given
        Review review = new Review();
        review.setUserId(USER_ID);
        review.setProductId(PRODUCT_ID);

        when(reviewRepository.findOneByUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        reviewService.create(review, null);

        // Then
        verify(reviewRepository).save(review);
        assertNotNull(review.getDateOfCreation());
        verify(productGrpcClient).productExists(PRODUCT_ID);
    }

    @Test
    void create_WhenReviewAlreadyExists_ThrowsReviewAlreadyExistsException() {
        // Given
        Review review = new Review();
        review.setUserId(USER_ID);
        review.setProductId(PRODUCT_ID);

        when(reviewRepository.findOneByUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(new Review()));

        // When & Then
        assertThrows(ReviewAlreadyExistsException.class,
                () -> reviewService.create(review, null));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void create_WhenProductNotExists_ThrowsProductNotFoundException() {
        // Given
        Review review = new Review();
        review.setUserId(USER_ID);
        review.setProductId(PRODUCT_ID);

        when(reviewRepository.findOneByUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(false);

        // When & Then
        assertThrows(ProductNotFoundException.class,
                () -> reviewService.create(review, null));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void create_WithTooManyImages_ThrowsTooManyImagesToUploadException() {
        // Given
        Review review = new Review();
        review.setUserId(USER_ID);
        review.setProductId(PRODUCT_ID);

        List<MultipartFile> images = List.of(
                mock(MultipartFile.class), mock(MultipartFile.class), mock(MultipartFile.class),
                mock(MultipartFile.class), mock(MultipartFile.class), mock(MultipartFile.class) // 6 images
        );

        when(reviewRepository.findOneByUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(productGrpcClient.productExists(PRODUCT_ID)).thenReturn(true);

        // When & Then
        assertThrows(TooManyImagesToUploadException.class,
                () -> reviewService.create(review, images));

        verify(reviewRepository, never()).save(any(Review.class));
    }

   /* @Test
    void edit_WithValidData_UpdatesReview() {
        // Given
        Review oldReview = new Review();
        oldReview.setId(REVIEW_ID);
        oldReview.setUserId(USER_ID);
        oldReview.setProductId(PRODUCT_ID);
        oldReview.setDateOfCreation(LocalDateTime.now().minusDays(1));
        oldReview.setEditingQuantity(0);
        oldReview.setPhotoFileNames(List.of("old_image1.jpg"));
        oldReview.setAnonymousReview(true);
        oldReview.setRating(3);
        oldReview.setUsagePeriod(UsagePeriod.LESS_THAN_MONTH);

        Review reviewToEdit = new Review();
        reviewToEdit.setId(REVIEW_ID);
        reviewToEdit.setRating(5);
        reviewToEdit.setComment("Updated comment");
        reviewToEdit.setAnonymousReview(false);
        reviewToEdit.setUsagePeriod(UsagePeriod.LESS_THAN_MONTH);

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(oldReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewToEdit);

        // When
        reviewService.edit(reviewToEdit, USER_ID, Optional.empty(), Optional.empty());

        // Then
        verify(reviewRepository).save(argThat(review ->
                review.getEditingQuantity() == 1 &&
                        review.getDateOfLastEditing() != null
        ));
    }

    @Test
    void edit_WhenUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        Review oldReview = new Review();
        oldReview.setId(REVIEW_ID);
        oldReview.setUserId(999L); // Different user

        Review reviewToEdit = new Review();
        reviewToEdit.setId(REVIEW_ID);

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(oldReview));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> reviewService.edit(reviewToEdit, USER_ID, Optional.empty(), Optional.empty()));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void edit_WhenCannotEdit_ThrowsRuntimeException() {
        // Given
        Review oldReview = new Review();
        oldReview.setId(REVIEW_ID);
        oldReview.setUserId(USER_ID);
        oldReview.setDateOfCreation(LocalDateTime.now().minusYears(4)); // Too old
        oldReview.setEditingQuantity(0);

        Review reviewToEdit = new Review();
        reviewToEdit.setId(REVIEW_ID);

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(oldReview));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reviewService.edit(reviewToEdit, USER_ID, Optional.empty(), Optional.empty()));

        assertEquals("Review cannot be edited anymore.", exception.getMessage());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void edit_WithImagesToDelete_DeletesImages() {
        // Given
        Review oldReview = new Review();
        oldReview.setId(REVIEW_ID);
        oldReview.setUserId(USER_ID);
        oldReview.setProductId(PRODUCT_ID);
        oldReview.setDateOfCreation(LocalDateTime.now().minusDays(1));
        oldReview.setEditingQuantity(0);
        oldReview.setPhotoFileNames(List.of("image1.jpg", "image2.jpg", "image3.jpg"));

        Review reviewToEdit = new Review();
        reviewToEdit.setId(REVIEW_ID);
        reviewToEdit.setRating(5);

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(oldReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewToEdit);

        List<String> imagesToDelete = List.of("image1.jpg", "image2.jpg");

        // When
        reviewService.edit(reviewToEdit, USER_ID, Optional.empty(), Optional.of(imagesToDelete));

        // Then
        verify(mediaKafkaClient).deleteMedia(imagesToDelete);
        verify(reviewRepository).save(argThat(review ->
                review.getPhotoFileNames().size() == 1 &&
                        review.getPhotoFileNames().contains("image3.jpg")
        ));
    }

    @Test
    void edit_WithNoChanges_ThrowsNoChangesInEditingReviewException() {
        // Given
        Review oldReview = new Review();
        oldReview.setId(REVIEW_ID);
        oldReview.setUserId(USER_ID);
        oldReview.setProductId(PRODUCT_ID);
        oldReview.setDateOfCreation(LocalDateTime.now().minusDays(1));
        oldReview.setEditingQuantity(0);
        oldReview.setRating(4);
        oldReview.setUsagePeriod(UsagePeriod.LESS_THAN_MONTH);
        oldReview.setAnonymousReview(false);
        oldReview.setAdvantages("Good quality");
        oldReview.setDisAdvantages("Expensive");
        oldReview.setComment("Nice product");
        oldReview.setPhotoFileNames(List.of("image1.jpg"));

        Review reviewToEdit = new Review();
        reviewToEdit.setId(REVIEW_ID);
        reviewToEdit.setRating(4);
        reviewToEdit.setUsagePeriod(UsagePeriod.LESS_THAN_MONTH);
        reviewToEdit.setAnonymousReview(false);
        reviewToEdit.setAdvantages("Good quality");
        reviewToEdit.setDisAdvantages("Expensive");
        reviewToEdit.setComment("Nice product");

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(oldReview));

        // When & Then
        assertThrows(NoChangesInEditingReviewException.class,
                () -> reviewService.edit(reviewToEdit, USER_ID, Optional.empty(), Optional.empty()));

        verify(reviewRepository, never()).save(any(Review.class));
    }*/

    @Test
    void checkEditingReviewAbility_WhenCanEdit_ReturnsCanEdit() {
        // Given
        Review review = new Review();
        review.setId(REVIEW_ID);
        review.setUserId(USER_ID);
        review.setDateOfCreation(LocalDateTime.now().minusMonths(1));
        review.setEditingQuantity(0);

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        // When
        EditReviewAbilityStatus result = reviewService.checkEditingReviewAbility(USER_ID, REVIEW_ID);

        // Then
        assertEquals(EditReviewAbilityStatus.CAN_EDIT, result);
    }

    @Test
    void checkEditingReviewAbility_WhenAttemptsExhausted_ReturnsAttemptsExhausted() {
        // Given
        Review review = new Review();
        review.setId(REVIEW_ID);
        review.setUserId(USER_ID);
        review.setDateOfCreation(LocalDateTime.now().minusMonths(1));
        review.setEditingQuantity(4);

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        // When
        EditReviewAbilityStatus result = reviewService.checkEditingReviewAbility(USER_ID, REVIEW_ID);

        // Then
        assertEquals(EditReviewAbilityStatus.ATTEMPTS_EXHAUSTED, result);
    }

    @Test
    void checkEditingReviewAbility_WhenTooOld_ReturnsTooOldReview() {
        // Given
        Review review = new Review();
        review.setId(REVIEW_ID);
        review.setUserId(USER_ID);
        review.setDateOfCreation(LocalDateTime.now().minusYears(4));
        review.setEditingQuantity(0);

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        // When
        EditReviewAbilityStatus result = reviewService.checkEditingReviewAbility(USER_ID, REVIEW_ID);

        // Then
        assertEquals(EditReviewAbilityStatus.TOO_OLD_REVIEW, result);
    }

    @Test
    void deleteByReviewId_WithValidData_DeletesReview() {
        // Given
        Review review = new Review();
        review.setId(REVIEW_ID);
        review.setUserId(USER_ID);
        review.setPhotoFileNames(List.of("image1.jpg", "image2.jpg"));

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        // When
        reviewService.deleteByReviewId(USER_ID, REVIEW_ID);

        // Then
        verify(reviewRepository).deleteById(REVIEW_ID);
        verify(mediaKafkaClient).deleteMedia(List.of("image1.jpg", "image2.jpg"));
    }

    @Test
    void deleteByProductId_WithValidData_DeletesReview() {
        // Given
        Review review = new Review();
        review.setId(REVIEW_ID);
        review.setUserId(USER_ID);
        review.setProductId(PRODUCT_ID);
        review.setPhotoFileNames(List.of("image1.jpg"));

        when(reviewRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(review));

        // When
        reviewService.deleteByProductId(USER_ID, PRODUCT_ID);

        // Then
        verify(reviewRepository).deleteById(REVIEW_ID);
        verify(mediaKafkaClient).deleteMedia(List.of("image1.jpg"));
    }
      @ParameterizedTest
      @EnumSource(ReviewSortType.class)
    void findAll_WithDifferentSortTypes_ReturnsReviews(ReviewSortType sortType) {
        // Given
        int offset = 0;
        Optional<Long> userId = sortType == ReviewSortType.MY_REVIEW ?
                Optional.of(USER_ID) : Optional.empty();

        List<Review> expectedReviews = List.of(new Review(), new Review());

        // Настраиваем только нужный стаб в зависимости от sortType
        switch (sortType) {
            case HIGH_RATING:
                when(reviewRepository.findByProductIdOrderByRatingDesc(eq(PRODUCT_ID), any(Pageable.class)))
                        .thenReturn(expectedReviews);
                break;
            case LOW_RATING:
                when(reviewRepository.findByProductIdOrderByRatingAsc(eq(PRODUCT_ID), any(Pageable.class)))
                        .thenReturn(expectedReviews);
                break;
            case NEWEST:
                when(reviewRepository.findByProductIdOrderByDateOfCreationDesc(eq(PRODUCT_ID), any(Pageable.class)))
                        .thenReturn(expectedReviews);
                break;
            case WITH_PHOTO:
                when(reviewRepository.findByProductIdOrderByPhotoPresence(eq(PRODUCT_ID), any(Pageable.class)))
                        .thenReturn(expectedReviews);
                break;
            case MY_REVIEW:
                when(reviewRepository.findOneByUserIdAndProductId(eq(USER_ID), eq(PRODUCT_ID)))
                        .thenReturn(Optional.of(new Review()));
                break;
        }

        // When
        List<Review> result = reviewService.findAll(PRODUCT_ID, offset, sortType, userId);

        // Then
        assertNotNull(result);
    }

    @Test
    void findAll_WithMyReviewAndNoUserId_ThrowsUserNotFoundException() {
        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> reviewService.findAll(PRODUCT_ID, 0, ReviewSortType.MY_REVIEW, Optional.empty()));
    }

    @Test
    void findAll_WithNegativeOffset_ThrowsIllegalArgumentException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.findAll(PRODUCT_ID, -1, ReviewSortType.NEWEST, Optional.empty()));
    }

    @Test
    void getProductsInfo_WithProductIds_ReturnsInfo() {
        // Given
        List<Long> productIds = List.of(100L, 200L, 300L);

        // Создаем моки для интерфейса ProductReviewStats
        ProductReviewStats stat1 = mock(ProductReviewStats.class);
        when(stat1.getProductId()).thenReturn(100L);
        when(stat1.getReviewCount()).thenReturn(5L);
        when(stat1.getAverageRating()).thenReturn(4.5);

        ProductReviewStats stat2 = mock(ProductReviewStats.class);
        when(stat2.getProductId()).thenReturn(200L);
        when(stat2.getReviewCount()).thenReturn(3L);
        when(stat2.getAverageRating()).thenReturn(3.8);

        when(reviewRepository.findReviewStatsByProductIds(productIds))
                .thenReturn(List.of(stat1, stat2));

        // When
        List<ProductReviewInfoDto> result = reviewService.getProductsInfo(productIds);

        // Then
        assertEquals(3, result.size());

        ProductReviewInfoDto info100 = result.stream()
                .filter(dto -> dto.getProductId().equals(100L))
                .findFirst().get();
        assertEquals(5L, info100.getReviewQuantity());
        assertEquals(4.5, info100.getAverageRating());

        ProductReviewInfoDto info200 = result.stream()
                .filter(dto -> dto.getProductId().equals(200L))
                .findFirst().get();
        assertEquals(3L, info200.getReviewQuantity());
        assertEquals(3.8, info200.getAverageRating());

        ProductReviewInfoDto info300 = result.stream()
                .filter(dto -> dto.getProductId().equals(300L))
                .findFirst().get();
        assertEquals(0L, info300.getReviewQuantity());
        assertEquals(0.0, info300.getAverageRating());
    }

    @Test
    void getProductInfo_WithExistingProduct_ReturnsInfo() {
        // Given
        ProductReviewStats stat = mock(ProductReviewStats.class);
        when(stat.getProductId()).thenReturn(PRODUCT_ID);
        when(stat.getReviewCount()).thenReturn(10L);
        when(stat.getAverageRating()).thenReturn(4.2);

        when(reviewRepository.findReviewStatsByProductIds(List.of(PRODUCT_ID)))
                .thenReturn(List.of(stat));

        // When
        ProductReviewInfoDto result = reviewService.getProductInfo(PRODUCT_ID);

        // Then
        assertEquals(PRODUCT_ID, result.getProductId());
        assertEquals(10L, result.getReviewQuantity());
        assertEquals(4.2, result.getAverageRating());
    }

    @Test
    void getProductInfo_WithNonExistingProduct_ReturnsZeroInfo() {
        // Given
        when(reviewRepository.findReviewStatsByProductIds(List.of(PRODUCT_ID)))
                .thenReturn(List.of());

        // When
        ProductReviewInfoDto result = reviewService.getProductInfo(PRODUCT_ID);

        // Then
        assertEquals(PRODUCT_ID, result.getProductId());
        assertEquals(0L, result.getReviewQuantity());
        assertEquals(0.0, result.getAverageRating());
    }

    @Test
    void getUserReviewByProductId_ReturnsReview() {
        // Given
        Review expectedReview = new Review();
        when(reviewRepository.findOneByUserIdAndProductId(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(expectedReview));

        // When
        Optional<Review> result = reviewService.getUserReviewByProductId(USER_ID, PRODUCT_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedReview, result.get());
    }

    @Test
    void getReviewByReviewId_ReturnsReview() {
        // Given
        Review expectedReview = new Review();
        when(reviewRepository.findById(REVIEW_ID))
                .thenReturn(Optional.of(expectedReview));

        // When
        Optional<Review> result = reviewService.getReviewByReviewId(REVIEW_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedReview, result.get());
    }

    @Test
    void validateEntityAndOwnership_WithValidOwnership_ReturnsReview() {
        // Given
        Review expectedReview = new Review();
        expectedReview.setId(REVIEW_ID);
        expectedReview.setUserId(USER_ID);

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(expectedReview));

        // When
        Review result = reviewService.validateEntityAndOwnership(USER_ID, REVIEW_ID);

        // Then
        assertEquals(expectedReview, result);
    }

    @Test
    void validateEntityAndOwnership_WhenReviewNotFound_ThrowsEntityNotFoundException() {
        // Given
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> reviewService.validateEntityAndOwnership(USER_ID, REVIEW_ID));
    }

    @Test
    void validateEntityAndOwnership_WhenUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        Review review = new Review();
        review.setId(REVIEW_ID);
        review.setUserId(999L); // Different user

        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> reviewService.validateEntityAndOwnership(USER_ID, REVIEW_ID));
    }
}
