package com.example.review_service.service;

import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.client.kafka.MediaKafkaClient;
import com.example.common.enumeration.media_service.BucketEnum;
import com.example.common.exception.UserNotFoundException;
import com.example.review_service.dto.ProductReviewInfoDto;
import com.example.review_service.dto.ProductReviewStats;
import com.example.review_service.entity.Review;
import com.example.review_service.enumeration.EditReviewAbilityStatus;
import com.example.review_service.enumeration.ReviewSortType;
import com.example.review_service.exception.NoChangesInEditingReviewException;
import com.example.review_service.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.common.service.CommonMediaService.validateImages;
import static com.example.review_service.enumeration.EditReviewAbilityStatus.*;
import static com.example.review_service.enumeration.RedisSubKeys.KAFKA_UPLOAD_IMAGES;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductGrpcClient productGrpcClient;

    private final ReviewRepository reviewRepository;

    private final MediaKafkaClient mediaKafkaClient;

    private final RedisService redisService;


    public void create(@NonNull Review productReview, List<MultipartFile> images){

          if (!productGrpcClient.productExists(productReview.getProductId())){
            throw new RuntimeException("Product does not exist");
          }
          productReview.setDateOfCreation(LocalDateTime.now());

          Review review = reviewRepository.save(productReview);

          if(images!=null && !images.isEmpty()){
              if(images.size()>5){
                  throw new RuntimeException("Too many images");
              }
              validateImages(images);
              String requestKey = UUID.randomUUID().toString();
              redisService.save(KAFKA_UPLOAD_IMAGES+":"+requestKey,review.getId().toString());
              mediaKafkaClient.sendSavingMediaRequest(images, BucketEnum.reviews,requestKey);
          }
    }
    @Transactional
    public void edit(@NonNull Review reviewToEdit,
                     @NonNull Long userId,
                     Optional<List<MultipartFile>> imageToSaveOptional,
                     Optional<List<String>> imagesToDeleteOptional){

        Review oldReview = reviewRepository.findById(reviewToEdit.getId())
                .orElseThrow(()->new RuntimeException("Review does not exist"));

        if(!oldReview.getUserId().equals(userId)){
            throw new RuntimeException("Review does not belong to user");
        }
        if(!checkEditingReviewAbility(oldReview.getId()).equals(CAN_EDIT))
            throw new RuntimeException("Review cannot be edited anymore.");

        reviewToEdit.setUserId(userId);
        reviewToEdit.setProductId(oldReview.getProductId());
        reviewToEdit.setDateOfCreation(oldReview.getDateOfCreation());

        List<String> imagesToDelete = new ArrayList<>(oldReview.getPhotoFileNames());
        imagesToDelete.retainAll(imagesToDeleteOptional.orElse(new ArrayList<>()));

        List<MultipartFile> imagesToSave = imageToSaveOptional.orElse(new ArrayList<>());

        if(!imagesToDelete.isEmpty()){
            mediaKafkaClient.deleteMedia(imagesToDelete);
            List<String> tempImgToSave = new ArrayList<>(oldReview.getPhotoFileNames());
            tempImgToSave.removeAll(imagesToDelete);
            reviewToEdit.setPhotoFileNames(tempImgToSave);

        } else{
            reviewToEdit.setPhotoFileNames(oldReview.getPhotoFileNames());
            if(imageToSaveOptional.isEmpty()
                    &&reviewToEdit.getUsagePeriod().equals(oldReview.getUsagePeriod())
                     &&reviewToEdit.getRating().equals(oldReview.getRating())
                      &&reviewToEdit.isAnonymousReview()==oldReview.isAnonymousReview()
                       &&Objects.equals(reviewToEdit.getAdvantages(), oldReview.getAdvantages())
                        &&Objects.equals(reviewToEdit.getDisAdvantages(), oldReview.getDisAdvantages())
                         &&Objects.equals(reviewToEdit.getComment(), oldReview.getComment())){
                            throw new NoChangesInEditingReviewException(oldReview.getId());
            }
        }
        reviewToEdit.setDateOfLastEditing(LocalDateTime.now());
        reviewToEdit.setEditingQuantity(oldReview.getEditingQuantity()+1);

        reviewRepository.save(reviewToEdit);

        if(!imagesToSave.isEmpty()){
            if((oldReview.getPhotoFileNames().size()-imagesToDelete.size()+imagesToSave.size())>5){
                throw new RuntimeException("Too many images to save");
            }
            validateImages(imagesToSave);
            String requestKey = UUID.randomUUID().toString();
            redisService.save(KAFKA_UPLOAD_IMAGES+":"+requestKey,oldReview.getId().toString());
            mediaKafkaClient.sendSavingMediaRequest(imagesToSave, BucketEnum.reviews,requestKey);
        }
    }

    public EditReviewAbilityStatus checkEditingReviewAbility(Long reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new RuntimeException("Review does not exist"));

        if(review.getEditingQuantity()>=4) return ATTEMPTS_EXHAUSTED;
        if(ChronoUnit.YEARS.between(review.getDateOfCreation(),LocalDateTime.now())>=3)
            return TOO_OLD_REVIEW;
        return CAN_EDIT;
    }

    @Transactional
    public void saveReviewImageFileNames(@NonNull List<String> newImageFileNames, @NonNull Long reviewId){

        if(newImageFileNames.size()>5){
            throw new RuntimeException("You can only save 5 images per one review");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new RuntimeException("Review does not exist"));

        int currentPhotoQuantity = review.getPhotoFileNames().size();

        if(currentPhotoQuantity>0){
            if((currentPhotoQuantity+newImageFileNames.size())>5){
                throw new RuntimeException("Review already has %d images, so you can't save %d new images"
                        .formatted(currentPhotoQuantity,newImageFileNames.size()));
            }
            newImageFileNames.addAll(review.getPhotoFileNames());

        }
        review.setPhotoFileNames(newImageFileNames);

        reviewRepository.save(review);
    }
    @Transactional
    public boolean deleteByReviewId(Long userId, Long reviewId){

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new RuntimeException("Review does not exist"));

        if(!review.getUserId().equals(userId)){
            return false;
        }
        reviewRepository.deleteById(review.getId());
        mediaKafkaClient.deleteMedia(review.getPhotoFileNames());
        return true;
    }

    @Transactional
    public boolean deleteByProductId(Long userId, Long productId){

        Review review = reviewRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(()->new RuntimeException("Review does not exist"));

        reviewRepository.deleteById(review.getId());
        mediaKafkaClient.deleteMedia(review.getPhotoFileNames());
        return true;
    }




    public List<Review> findAll(Long productId, int offset, ReviewSortType sortType, Optional<Long> userId) throws UserNotFoundException {

        if(offset<0) throw new IllegalArgumentException("offset must be greater than 0");

        int limit = 40;

        Pageable pageable = PageRequest.of(offset/limit, limit);

        return switch(sortType){
            case HIGH_RATING -> reviewRepository.findByProductIdOrderByRatingDesc(productId, pageable);
            case LOW_RATING -> reviewRepository.findByProductIdOrderByRatingAsc(productId, pageable);
            case NEWEST -> reviewRepository.findByProductIdOrderByDateOfCreationDesc(productId, pageable);
            case WITH_PHOTO -> reviewRepository.findByProductIdOrderByPhotoPresence(productId, pageable);
            case MY_REVIEW -> {
                if(userId.isEmpty()) throw new UserNotFoundException();
                yield reviewRepository.findOneByUserIdAndProductId(userId.get(), productId)
                        .map(List::of)
                        .orElse(List.of());
            }
            default -> throw new RuntimeException("Sort type is not recognized");
        };
    }

    public List<ProductReviewInfoDto> getProductsInfo(List<Long> productIds) {

        List<ProductReviewStats> existingStats = reviewRepository.findReviewStatsByProductIds(productIds);

        Map<Long,ProductReviewInfoDto > resultMap = existingStats.stream()
                .collect(Collectors.toMap(
                        ProductReviewStats::getProductId,
                        stat -> new ProductReviewInfoDto(stat.getProductId(),stat.getReviewCount(), stat.getAverageRating())
                ));

        // Добавляю нулевые значения для всех productIds
        for (Long productId : productIds) {
            resultMap.putIfAbsent(productId, new ProductReviewInfoDto(productId,0L, 0));
        }

        return new ArrayList<>(resultMap.values());
    }

    public ProductReviewInfoDto getProductInfo(Long productId) {

        List<ProductReviewStats> existingStats = reviewRepository.findReviewStatsByProductIds(List.of(productId));

        if(existingStats.isEmpty()){
            return new ProductReviewInfoDto(productId,0L, 0);
        }
        var stat = existingStats.get(0);

        return new ProductReviewInfoDto(stat.getProductId(),stat.getReviewCount(), stat.getAverageRating());
    }

    public Optional<Review> getUserReviewByProductId(Long userId, Long productId) {
        return reviewRepository.findOneByUserIdAndProductId(userId, productId);
    }

    public Optional<Review> getReviewByReviewId(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }









}
