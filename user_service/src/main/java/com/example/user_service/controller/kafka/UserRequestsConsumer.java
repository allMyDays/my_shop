package com.example.user_service.controller.kafka;

import com.example.common.dto.media.kafka.SavedMediaFilesResponseDTO;

import com.example.user_service.service.RedisService;
import com.example.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.common.enumeration.media_service.BucketEnum.users;
import static com.example.common.constant.kafka.Topics.MEDIA_RESPONSE_TOPIC;
import static com.example.common.constant.kafka.Topics.USER_REQUEST_TOPIC;
import static com.example.common.constant.kafka.keys.MediaKeys.SAVED_MEDIA_FILES;
import static com.example.user_service.enumeration.RedisSubKeys.KAFKA_UPLOAD_AVATAR;

@Service
@RequiredArgsConstructor
public class UserRequestsConsumer {

    private final UserService userService;

    private final ObjectMapper objectMapper;

    private final RedisService redisService;

    @KafkaListener(topics = {USER_REQUEST_TOPIC, MEDIA_RESPONSE_TOPIC}, groupId = "${spring.kafka.consumer.group-id}")
    public void handleUsersRequests(ConsumerRecord<String, String> record){

        String key = record.key();
        String rawJsonString = record.value();

        try{

         switch (key){
             case SAVED_MEDIA_FILES:
                 var savedFileDto = objectMapper.readValue(rawJsonString, SavedMediaFilesResponseDTO.class);
                  if(savedFileDto.getBucket().equals(users)){
                      Optional<String> userId = redisService.get(KAFKA_UPLOAD_AVATAR+":"+savedFileDto.getRequestKey(),true);
                      userId.ifPresent(s -> userService.saveUserAvatar(savedFileDto.getGeneratedFileKeys().get(0), Long.parseLong(s)));
                 }
                 break;


          }
        }
     catch (Exception e){
            //todo

     }

    }
}
