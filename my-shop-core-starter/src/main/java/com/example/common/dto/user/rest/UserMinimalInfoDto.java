package com.example.common.dto.user.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserMinimalInfoDto {

    private Long userId;

    private String userVisibleName;

    private String avatarFileName;

}
