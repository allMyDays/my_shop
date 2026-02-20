package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class VerifyEmailRequestDTO {

    @Email
    private String Email;

    private String UserCode;

    public void setEmail(String email) {
        Email = email==null?null:email.trim();
    }

    public void setUserCode(String userCode) {
        UserCode = userCode==null?null:userCode.trim();
    }

    public VerifyEmailRequestDTO(String email, String userCode) {
        Email = email==null?null:email.trim();
        UserCode = userCode==null?null:userCode.trim();
    }
}
