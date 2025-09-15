package com.example.user_service.controller.kafka;

import com.example.common.kafka.dto.user.UserDeleteAvatarDto;
import com.example.common.kafka.dto.user.UserUpdateAvatarDto;
import com.example.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import static com.example.common.kafka.constant.Topics.USER_TOPIC;
import static com.example.common.kafka.constant.keys.UserKeys.DELETE_USER_AVATAR;
import static com.example.common.kafka.constant.keys.UserKeys.UPDATE_USER_AVATAR;

@Service
@RequiredArgsConstructor
public class UserRequestsConsumer {

    private final UserService userService;

    private final ObjectMapper objectMapper;


    @KafkaListener(topics = USER_TOPIC , groupId = "${spring.kafka.consumer.group-id}")
    public void handleUsersRequests(ConsumerRecord<String, String> record){

        String key = record.key();
        String rawJsonString = record.value();

        try{
         switch (key){
            case UPDATE_USER_AVATAR:
                var updateDto = objectMapper.readValue(rawJsonString, UserUpdateAvatarDto.class);
                userService.updateUserAvatar(updateDto.getUserId(), updateDto.getNewAvatarFileName());
                break;


            case DELETE_USER_AVATAR:
                var deleteDto = objectMapper.readValue(rawJsonString, UserDeleteAvatarDto.class);
                userService.deleteUserAvatar(deleteDto.getUserId());
                break;

          }
        }
     catch (Exception e){
            //todo

     }

    }
}
