package com.example.catalogue_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String name;

    @Size(min = 5, max = 25, message = "Password must be at least 5 characters long")
    private String password;

    @Size(min = 5, max = 25, message = "Password must be at least 5 characters long")
    private String matchingPassword;

    @Email
    private String email;

    private String phoneNumber;




}