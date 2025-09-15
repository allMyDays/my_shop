package com.example.common.client.kafka;

import com.example.common.kafka.dto.user.UserDeleteAvatarDto;
import com.example.common.kafka.dto.user.UserUpdateAvatarDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import static com.example.common.kafka.constant.Topics.USER_TOPIC;
import static com.example.common.kafka.constant.keys.UserKeys.DELETE_USER_AVATAR;
import static com.example.common.kafka.constant.keys.UserKeys.UPDATE_USER_AVATAR;

@Service
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class UserKafkaClient {

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @Lazy
    public void setKafkaTemplate(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void updateUserAvatar(long userId, String avatarFileName) {
        UserUpdateAvatarDto updateAvatarDto = new UserUpdateAvatarDto();
        updateAvatarDto.setNewAvatarFileName(avatarFileName);
        updateAvatarDto.setUserId(userId);

        kafkaTemplate.send(USER_TOPIC, UPDATE_USER_AVATAR, updateAvatarDto);


    }

    public void deleteUserAvatar(Long userId) {
        UserDeleteAvatarDto userDeleteAvatarDto = new UserDeleteAvatarDto();
        userDeleteAvatarDto.setUserId(userId);

        kafkaTemplate.send(USER_TOPIC, DELETE_USER_AVATAR, userDeleteAvatarDto);

    }









}
