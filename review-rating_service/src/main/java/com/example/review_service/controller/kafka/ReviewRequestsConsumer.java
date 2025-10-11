package com.example.review_service.controller.kafka;

import com.example.common.dto.media.kafka.SavedMediaFilesResponseDTO;
import com.example.review_service.service.RedisService;
import com.example.review_service.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.example.common.constant.kafka.Topics.MEDIA_RESPONSE_TOPIC;
import static com.example.common.constant.kafka.Topics.USER_REQUEST_TOPIC;
import static com.example.common.constant.kafka.keys.MediaKeys.SAVED_MEDIA_FILES;
import static com.example.common.enumeration.media_service.BucketEnum.reviews;
import static com.example.common.enumeration.media_service.BucketEnum.users;
import static com.example.review_service.enumeration.RedisSubKeys.KAFKA_UPLOAD_IMAGES;

@Service
@RequiredArgsConstructor
public class ReviewRequestsConsumer {

    private final ReviewService reviewService;

    private final ObjectMapper objectMapper;

    private final RedisService redisService;

    @KafkaListener(topics = {MEDIA_RESPONSE_TOPIC}, groupId = "${spring.kafka.consumer.group-id}")
    public void handleUsersRequests(ConsumerRecord<String, String> record){

        String key = record.key();
        String rawJsonString = record.value();

        try{

         switch (key){
             case SAVED_MEDIA_FILES:
                 var savedFileDto = objectMapper.readValue(rawJsonString, SavedMediaFilesResponseDTO.class);
                  if(savedFileDto.getBucket().equals(reviews)){
                      String reviewId = redisService.get(KAFKA_UPLOAD_IMAGES+":"+savedFileDto.getRequestKey());
                      if(reviewId != null){
                          reviewService.saveReviewImageFileNames(savedFileDto.getGeneratedFileKeys(),Long.parseLong(reviewId));

                       }
                 }
                 break;


          }
        }
     catch (Exception e){
            //todo

     }

    }
}
