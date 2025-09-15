package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class VerifyEmailRequestDTO {

    @Email
    private String Email;

    private String UserCode;



}
