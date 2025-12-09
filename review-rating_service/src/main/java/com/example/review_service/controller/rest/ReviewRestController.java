package com.example.review_service.controller.rest;

import com.example.common.exception.UserNotFoundException;
import com.example.review_service.controller.rest.i.IReviewRestController;
import com.example.review_service.dto.EditReviewRequestDto;
import com.example.review_service.dto.ProductReviewInfoDto;
import com.example.review_service.dto.CreateReviewRequestDto;
import com.example.review_service.dto.ReviewResponseDto;
import com.example.review_service.entity.Review;
import com.example.review_service.enumeration.EditReviewAbilityStatus;
import com.example.review_service.enumeration.ReviewSortType;
import com.example.review_service.exception.NoChangesInEditingReviewException;
import com.example.review_service.exception.ReviewAlreadyExistsException;
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
public class ReviewRestController implements IReviewRestController {

    private final ReviewService reviewService;

    private final ReviewMapper reviewMapper;


    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReview(
            @Validated @ModelAttribute CreateReviewRequestDto productReviewDto,
            BindingResult bindingResult,
            @RequestPart(name = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList());
        }

        Review review = reviewMapper.toReviewEntity(productReviewDto);

        review.setUserId(getMyUserEntityId(jwt));

        try{
            reviewService.create(review, images);

        }catch (ReviewAlreadyExistsException e){
            return ResponseEntity.status(409)
                    .body(e.getMessage());
        }

        return ResponseEntity.ok().build();

    }

    @PutMapping(value = "/edit", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> editReview(
            @Validated @ModelAttribute EditReviewRequestDto reviewDto,
            BindingResult bindingResult,
            @RequestPart(name = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList());
        }
        try{
         reviewService.edit(
                reviewMapper.toReviewEntity(reviewDto),
                getMyUserEntityId(jwt),
                Optional.ofNullable(images),
                Optional.ofNullable(reviewDto.getDeletedPhotos()));
        }catch (NoChangesInEditingReviewException e){
            return ResponseEntity.status(409)
                 .body(e.getMessage());
        }

        return ResponseEntity.ok()
                .build();

    }
    @GetMapping("/edit-ability")
    @PreAuthorize("isAuthenticated()")
    public EditReviewAbilityStatus checkEditingReviewAbility(@RequestParam Long reviewId, @AuthenticationPrincipal Jwt jwt){

        return reviewService.checkEditingReviewAbility(getMyUserEntityId(jwt), reviewId);
    }

    @DeleteMapping("/{reviewId:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeReviewByReviewId(@PathVariable Long reviewId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        reviewService.deleteByReviewId(getMyUserEntityId(jwt),reviewId);

        return ResponseEntity.ok()
                    .build();


    }

    @DeleteMapping("/by_product_id/{productId:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeReviewByProductId(@PathVariable Long productId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        reviewService.deleteByProductId(getMyUserEntityId(jwt),productId);

        return ResponseEntity.ok()
                    .build();


    }




    @GetMapping("/{productId:\\d+}")
    public List<ReviewResponseDto> getReviews(@PathVariable long productId,
                                              @RequestParam(defaultValue = "HIGH_RATING") ReviewSortType sortType,
                                              @RequestParam int offset, // возвращает по 40 отзывов начиная с offset
                                              @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

                 return reviewMapper.toReviewResponseDtoList(reviewService.findAll(
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

    @GetMapping("/get_minimal_data/{reviewId:\\d+}")

    public ResponseEntity<?> getReview(@PathVariable Long reviewId) {

        Optional<Review> reviewOptional = reviewService.getReviewByReviewId(reviewId);

        if (reviewOptional.isEmpty())
            return ResponseEntity.badRequest()
                .body("Не удалось получить отзыв с id "+reviewId);

        return ResponseEntity
                .ok(reviewMapper.toReviewResponseDtoWithNoUserNameAndAvatar(reviewOptional.get()));

    }


}
