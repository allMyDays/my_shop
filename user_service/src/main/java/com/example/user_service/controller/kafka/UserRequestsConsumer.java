package com.example.user_service.controller.kafka;


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

@Service
@RequiredArgsConstructor
public class UserRequestsConsumer {

    private final UserService userService;

    private final ObjectMapper objectMapper;

    private final RedisService redisService;

    @KafkaListener(topics = {USER_REQUEST_TOPIC}, groupId = "${spring.kafka.consumer.group-id}")
    public void handleUsersRequests(ConsumerRecord<String, String> record){

        String key = record.key();
        String rawJsonString = record.value();

        try{

         switch (key){}
        }
     catch (Exception e){
            //todo

     }

    }
}
