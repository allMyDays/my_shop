package com.example.common.kafka.dto.user;

import lombok.Data;

@Data
public class UserUpdateAvatarDto {

    private Long userId;

    private String newAvatarFileName;



}
