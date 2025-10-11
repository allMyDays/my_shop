package com.example.review_service.controller.rest;

import com.example.common.exception.UserNotFoundException;
import com.example.review_service.dto.ProductReviewInfoDto;
import com.example.review_service.dto.ReviewRequestDto;
import com.example.review_service.dto.ReviewResponseDto;
import com.example.review_service.entity.Review;
import com.example.review_service.enumeration.ReviewSortType;
import com.example.review_service.mapper.ReviewMapper;
import com.example.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewRestController {

    private final ReviewService reviewService;

    private final ReviewMapper reviewMapper;


    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReview(
            @Validated @ModelAttribute ReviewRequestDto productReviewDto,
            BindingResult bindingResult,
            @RequestPart(name = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList());
        }
        Long userId = getMyUserEntityId(jwt);

        if(reviewService.getUserReviewByProductId(userId, productReviewDto.getProductId()).isPresent()) {
            return ResponseEntity.status(409)
                    .body("Отзыв пользоваля с id %d уже существует у продукта с id %d".formatted(userId, productReviewDto.getProductId()));
        }

        Review review = reviewMapper.toReviewEntity(productReviewDto);

        review.setUserId(userId);

        reviewService.create(review, images);

        return ResponseEntity.ok(productReviewDto);

    }

    @DeleteMapping("/{reviewId:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeReviewByReviewId(@PathVariable Long reviewId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        if(reviewService.deleteByReviewId(getMyUserEntityId(jwt),reviewId)){
            return ResponseEntity.ok()
                    .build();
        }
        return ResponseEntity.badRequest()
                .body("Не удалось удалить отзыв. Убедитесь, что этот отзыв принадлежит вам.");

    }

    @DeleteMapping("/by_product_id/{productId:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeReviewByProductId(@PathVariable Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        if(reviewService.deleteByProductId(getMyUserEntityId(jwt),productId)){
            return ResponseEntity.ok()
                    .build();
        }
        return ResponseEntity.badRequest()
                .body("Не удалось удалить отзыв. Убедитесь, что вы оставляли отзыв к данному продукту.");

    }




    @GetMapping("/{productId:\\d+}")
    public List<ReviewResponseDto> getReviews(@PathVariable long productId,
                                              @RequestParam(defaultValue = "HIGH_RATING") ReviewSortType sortType,
                                              @RequestParam int offset, // возвращает по 40 отзывов начиная с offset
                                              @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

                 return reviewMapper.toReviewResponseDto(reviewService.findAll(
                         productId,
                         offset,
                         sortType,
                         jwt==null?Optional.empty():Optional.of(getMyUserEntityId(jwt)))
                 );
    }


    @PostMapping("/get_products_info")
    public List<ProductReviewInfoDto> getProductsInfo(@RequestBody List<Long> productIds)  {
       return reviewService.getProductsInfo(productIds);
    }

    @GetMapping("/get_product_info")
    public ProductReviewInfoDto getProductInfo(@RequestParam Long productId)  {
        return reviewService.getProductInfo(productId);
    }


}
