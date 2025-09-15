package com.example.support_service.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateChatDto {

    @Size(min = 4, max = 30)
    private String topic;


}
