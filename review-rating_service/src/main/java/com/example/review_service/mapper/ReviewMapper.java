package com.example.review_service.mapper;

import com.example.common.client.grpc.UserGrpcClient;
import com.example.common.dto.user.rest.UserMinimalInfoDto;
import com.example.review_service.dto.ReviewRequestDto;
import com.example.review_service.dto.ReviewResponseDto;
import com.example.review_service.entity.Review;
import com.example.review_service.enumeration.UsagePeriod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public abstract class ReviewMapper {

    @Autowired
    protected UserGrpcClient userGrpcClient;


    public abstract Review toReviewEntity(ReviewRequestDto reviewRequestDto);

    @Mapping(target = "usagePeriod", expression = "java(mapUsagePeriod(review.getUsagePeriod()))")
    protected abstract ReviewResponseDto toReviewResponseDto(Review review);



    public List<ReviewResponseDto> toReviewResponseDto(List<Review> reviews){

        List<Long> userIds = reviews.stream()
                .filter(a->!a.isAnonymousReview())
                .map(Review::getUserId)
                .toList();

        List<UserMinimalInfoDto> userMinimalInfoDTOs = userGrpcClient.getUserMinimalInfo(userIds);

        Map<Long, UserMinimalInfoDto> userMinimalInfoMap = userMinimalInfoDTOs.stream()
                .collect(Collectors.toMap(UserMinimalInfoDto::getUserId, Function.identity()));

        List<ReviewResponseDto> reviewDtoList = new ArrayList<>();

        for(Review review: reviews){
            ReviewResponseDto reviewDto = toReviewResponseDto(review);
            UserMinimalInfoDto userMinimalInfoDto = userMinimalInfoMap.get(review.getUserId());
            if (userMinimalInfoDto!=null){
             reviewDto.setUserVisibleName(userMinimalInfoDto.getUserVisibleName());
             reviewDto.setUserAvatarFileName(userMinimalInfoDto.getAvatarFileName());
            }else{
               if(review.isAnonymousReview()){
                   reviewDto.setUserVisibleName("анонимный пользователь");
               }else{
                   reviewDto.setUserVisibleName("неизвестный пользователь");
               }

            } reviewDtoList.add(reviewDto);
        }
        return reviewDtoList;


    }

    protected String mapUsagePeriod(UsagePeriod usagePeriod){
        return switch (usagePeriod){
            case LESS_THAN_WEEK -> "менее недели";
            case LESS_THAN_MONTH -> "менее месяца";
            case LESS_THAN_HALF_YEAR -> "менее полугода";
            case LESS_THAN_YEAR -> "менее года";
            case LESS_THAN_2_YEARS -> "менее двух лет";
            case MORE_THAN_2_YEARS -> "более двух лет";
        };



    }




}
