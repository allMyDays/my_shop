package com.example.common.dto.user.rest;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    private Long id;

    private String nickName;

    private String email;

    private String firstName;

    private String lastName;

    private String avatarFileName;


}

