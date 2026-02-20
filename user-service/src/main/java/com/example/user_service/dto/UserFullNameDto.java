package com.example.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserFullNameDto {

    private String keycloakId;

    private String firstName;

    private String lastName;


}
