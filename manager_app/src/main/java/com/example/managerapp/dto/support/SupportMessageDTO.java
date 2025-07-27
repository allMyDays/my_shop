package com.example.managerapp.dto.support;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SupportMessageDTO {


    private Long chatId;
    private boolean isUserMessage;
    private String message;
    private LocalDateTime dateOfCreation;





}
