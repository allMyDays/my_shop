package com.example.review_service.unit.controller;

import com.example.common.exception.UserNotFoundException;
import com.example.review_service.controller.rest.ReviewRestController;
import com.example.review_service.dto.CreateReviewRequestDto;
import com.example.review_service.dto.EditReviewRequestDto;
import com.example.review_service.dto.ProductReviewInfoDto;
import com.example.review_service.dto.ReviewResponseDto;
import com.example.review_service.entity.Review;
import com.example.review_service.enumeration.EditReviewAbilityStatus;
import com.example.review_service.enumeration.ReviewSortType;
import com.example.review_service.exception.NoChangesInEditingReviewException;
import com.example.review_service.exception.ReviewAlreadyExistsException;
import com.example.review_service.mapper.ReviewMapper;
import com.example.review_service.service.ReviewService;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewRestControllerUnitTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private Jwt jwt;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ReviewRestController reviewRestController;

    private final Long TEST_USER_ID = 123L;
    private final Long TEST_REVIEW_ID = 456L;
    private final Long TEST_PRODUCT_ID = 789L;


    // Tests for createReview method
    @Test
    void createReview_WhenValidRequest_ShouldReturnOk() throws UserNotFoundException, ReviewAlreadyExistsException {
        // Arrange
        CreateReviewRequestDto reviewDto = new CreateReviewRequestDto();
        List<MultipartFile> images = List.of(multipartFile);
        Review review = new Review();
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(reviewMapper.toReviewEntity(reviewDto)).thenReturn(review);
        doNothing().when(reviewService).create(eq(review), eq(images));

        // Act
        ResponseEntity<?> response = reviewRestController.createReview(reviewDto, bindingResult, images, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewMapper).toReviewEntity(reviewDto);
        verify(reviewService).create(review, images);
        assertEquals(TEST_USER_ID, review.getUserId());
    }

    @Test
    void createReview_WhenValidationErrors_ShouldReturnBadRequest() throws UserNotFoundException {
        // Arrange
        CreateReviewRequestDto reviewDto = new CreateReviewRequestDto();
        List<MultipartFile> images = List.of(multipartFile);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors())
                .thenReturn(List.of(new ObjectError("rating", "Rating is required")));

        // Act
        ResponseEntity<?> response = reviewRestController.createReview(reviewDto, bindingResult, images, jwt);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        verify(reviewService, never()).create(any(), any());
    }

    @Test
    void createReview_WhenReviewAlreadyExists_ShouldReturnConflict() throws UserNotFoundException, ReviewAlreadyExistsException {
        // Arrange
        CreateReviewRequestDto reviewDto = new CreateReviewRequestDto();
        List<MultipartFile> images = List.of(multipartFile);
        Review review = new Review();

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(reviewMapper.toReviewEntity(reviewDto)).thenReturn(review);
        doThrow(new ReviewAlreadyExistsException(TEST_USER_ID, TEST_PRODUCT_ID))
                .when(reviewService).create(eq(review), eq(images));

        // Act
        ResponseEntity<?> response = reviewRestController.createReview(reviewDto, bindingResult, images, jwt);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Отзыв пользоваля с id %d уже существует у продукта с id %d".formatted(TEST_USER_ID, TEST_PRODUCT_ID), response.getBody());
    }

    @Test
    void createReview_WhenNoImages_ShouldHandleNullImages() throws UserNotFoundException, ReviewAlreadyExistsException {
        // Arrange
        CreateReviewRequestDto reviewDto = new CreateReviewRequestDto();
        Review review = new Review();

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(reviewMapper.toReviewEntity(reviewDto)).thenReturn(review);

        // Act
        ResponseEntity<?> response = reviewRestController.createReview(reviewDto, bindingResult, null, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).create(eq(review), isNull());
    }

    // Tests for editReview method
 /*   @Test
    void editReview_WhenValidRequest_ShouldReturnOk() throws UserNotFoundException, NoChangesInEditingReviewException {
        // Arrange
        EditReviewRequestDto reviewDto = new EditReviewRequestDto();
        List<MultipartFile> images = List.of(multipartFile);
        Review review = new Review();

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(reviewMapper.toReviewEntity(reviewDto)).thenReturn(review);
        doNothing().when(reviewService).edit(eq(review), eq(TEST_USER_ID), any(), any());

        // Act
        ResponseEntity<?> response = reviewRestController.editReview(reviewDto, bindingResult, images, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).edit(review, TEST_USER_ID, Optional.of(images), Optional.empty());
    }

    @Test
    void editReview_WhenNoChanges_ShouldReturnConflict() throws UserNotFoundException, NoChangesInEditingReviewException {
        // Arrange
        EditReviewRequestDto reviewDto = new EditReviewRequestDto();
        List<MultipartFile> images = List.of(multipartFile);
        Review review = new Review();

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(reviewMapper.toReviewEntity(reviewDto)).thenReturn(review);
        doThrow(new NoChangesInEditingReviewException(0000L))
                .when(reviewService).edit(eq(review), eq(TEST_USER_ID), any(), any());

        // Act
        ResponseEntity<?> response = reviewRestController.editReview(reviewDto, bindingResult, images, jwt);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Отзыв с id 0 уже имеет указанные параметры.", response.getBody());
    }

    @Test
    void editReview_WithDeletedPhotos_ShouldPassToService() throws UserNotFoundException, NoChangesInEditingReviewException {
        // Arrange
        EditReviewRequestDto reviewDto = new EditReviewRequestDto();
        reviewDto.setDeletedPhotos(List.of("photo1.jpg", "photo2.jpg"));
        List<MultipartFile> images = List.of(multipartFile);
        Review review = new Review();

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(reviewMapper.toReviewEntity(reviewDto)).thenReturn(review);

        // Act
        ResponseEntity<?> response = reviewRestController.editReview(reviewDto, bindingResult, images, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).edit(review, TEST_USER_ID, Optional.of(images), Optional.of(List.of("photo1.jpg", "photo2.jpg")));
    }*/

    // Tests for checkEditingReviewAbility method
    @Test
    void checkEditingReviewAbility_ShouldReturnStatusFromService() {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(reviewService.checkEditingReviewAbility(eq(TEST_USER_ID), eq(TEST_REVIEW_ID)))
                .thenReturn(EditReviewAbilityStatus.CAN_EDIT);

        // Act
        EditReviewAbilityStatus result = reviewRestController.checkEditingReviewAbility(TEST_REVIEW_ID, jwt);

        // Assert
        assertEquals(EditReviewAbilityStatus.CAN_EDIT, result);
        verify(reviewService).checkEditingReviewAbility(TEST_USER_ID, TEST_REVIEW_ID);
    }

    // Tests for removeReviewByReviewId method
    @Test
    void removeReviewByReviewId_WhenValidRequest_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doNothing().when(reviewService).deleteByReviewId(eq(TEST_USER_ID), eq(TEST_REVIEW_ID));

        // Act
        ResponseEntity<?> response = reviewRestController.removeReviewByReviewId(TEST_REVIEW_ID, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).deleteByReviewId(TEST_USER_ID, TEST_REVIEW_ID);
    }

    @Test
    void removeReviewByReviewId_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doThrow(new UserNotFoundException())
                .when(reviewService).deleteByReviewId(eq(TEST_USER_ID), eq(TEST_REVIEW_ID));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            reviewRestController.removeReviewByReviewId(TEST_REVIEW_ID, jwt);
        });
    }

    // Tests for removeReviewByProductId method
    @Test
    void removeReviewByProductId_WhenValidRequest_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doNothing().when(reviewService).deleteByProductId(eq(TEST_USER_ID), eq(TEST_PRODUCT_ID));

        // Act
        ResponseEntity<?> response = reviewRestController.removeReviewByProductId(TEST_PRODUCT_ID, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).deleteByProductId(TEST_USER_ID, TEST_PRODUCT_ID);
    }

    // Tests for getReviews method
    @Test
    void getReviews_WhenAuthenticated_ShouldReturnReviews() throws UserNotFoundException {
        // Arrange
        List<Review> reviews = List.of(new Review(), new Review());
        List<ReviewResponseDto> expectedDtos = List.of(new ReviewResponseDto(), new ReviewResponseDto());

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());

        when(reviewService.findAll(eq(TEST_PRODUCT_ID), eq(0), eq(ReviewSortType.HIGH_RATING), eq(Optional.of(TEST_USER_ID))))
                .thenReturn(reviews);
        when(reviewMapper.toReviewResponseDtoList(eq(reviews))).thenReturn(expectedDtos);

        // Act
        List<ReviewResponseDto> result = reviewRestController.getReviews(TEST_PRODUCT_ID, ReviewSortType.HIGH_RATING, 0, jwt);

        // Assert
        assertEquals(expectedDtos, result);
        verify(reviewService).findAll(TEST_PRODUCT_ID, 0, ReviewSortType.HIGH_RATING, Optional.of(TEST_USER_ID));
    }

    @Test
    void getReviews_WhenNotAuthenticated_ShouldReturnReviewsWithEmptyUserId() throws UserNotFoundException {
        // Arrange
        List<Review> reviews = List.of(new Review());
        List<ReviewResponseDto> expectedDtos = List.of(new ReviewResponseDto());

        when(reviewService.findAll(eq(TEST_PRODUCT_ID), eq(10), eq(ReviewSortType.LOW_RATING), eq(Optional.empty())))
                .thenReturn(reviews);
        when(reviewMapper.toReviewResponseDtoList(eq(reviews))).thenReturn(expectedDtos);

        // Act
        List<ReviewResponseDto> result = reviewRestController.getReviews(TEST_PRODUCT_ID, ReviewSortType.LOW_RATING, 10, null);

        // Assert
        assertEquals(expectedDtos, result);
        verify(reviewService).findAll(TEST_PRODUCT_ID, 10, ReviewSortType.LOW_RATING, Optional.empty());
    }

    // Tests for getProductsInfo method
    @Test
    void getProductsInfo_ShouldReturnProductsInfo() {
        // Arrange
        List<Long> productIds = List.of(1L, 2L, 3L);
        List<ProductReviewInfoDto> expectedInfo = List.of(
                new ProductReviewInfoDto(0L, 0L, 0),
                new ProductReviewInfoDto(0L, 0L, 0),
                new ProductReviewInfoDto(0L, 0L, 0)
        );

        when(reviewService.getProductsInfo(eq(productIds))).thenReturn(expectedInfo);

        // Act
        List<ProductReviewInfoDto> result = reviewRestController.getProductsInfo(productIds);

        // Assert
        assertEquals(expectedInfo, result);
        verify(reviewService).getProductsInfo(productIds);


    }

    @Test
    void getProductsInfo_WhenEmptyList_ShouldReturnEmptyList() {
        // Arrange
        List<Long> productIds = List.of();
        List<ProductReviewInfoDto> expectedInfo = List.of();

        when(reviewService.getProductsInfo(eq(productIds))).thenReturn(expectedInfo);

        // Act
        List<ProductReviewInfoDto> result = reviewRestController.getProductsInfo(productIds);

        // Assert
        assertTrue(result.isEmpty());
        verify(reviewService).getProductsInfo(productIds);
    }

    // Tests for getProductInfo method
    @Test
    void getProductInfo_ShouldReturnProductInfo() {
        // Arrange
        ProductReviewInfoDto expectedInfo = new ProductReviewInfoDto(0L, 0L, 0);
        when(reviewService.getProductInfo(eq(TEST_PRODUCT_ID))).thenReturn(expectedInfo);

        // Act
        ProductReviewInfoDto result = reviewRestController.getProductInfo(TEST_PRODUCT_ID);

        // Assert
        assertEquals(expectedInfo, result);
        verify(reviewService).getProductInfo(TEST_PRODUCT_ID);
    }

    // Tests for getReview method
    @Test
    void getReview_WhenReviewExists_ShouldReturnReview() {
        // Arrange
        Review review = new Review();
        ReviewResponseDto expectedDto = new ReviewResponseDto();

        when(reviewService.getReviewByReviewId(eq(TEST_REVIEW_ID))).thenReturn(Optional.of(review));
        when(reviewMapper.toReviewResponseDtoWithNoUserNameAndAvatar(eq(review))).thenReturn(expectedDto);

        // Act
        ResponseEntity<?> response = reviewRestController.getReview(TEST_REVIEW_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDto, response.getBody());
    }

    @Test
    void getReview_WhenReviewNotFound_ShouldReturnBadRequest() {
        // Arrange
        when(reviewService.getReviewByReviewId(eq(TEST_REVIEW_ID))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reviewRestController.getReview(TEST_REVIEW_ID);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Не удалось получить отзыв с id " + TEST_REVIEW_ID, response.getBody());
    }

}