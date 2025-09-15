package com.example.common.dto.support;


import com.example.common.security.XssSanitizer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class SupportMessageResponseDTO {


    @Setter
    private Long chatId;

    @Setter
    private boolean isUserMessage;

    private String message;

    @Setter
    private LocalDateTime dateOfCreation;


    public SupportMessageResponseDTO(Long chatId, boolean isUserMessage, String message, LocalDateTime dateOfCreation) {
        this.chatId = chatId;
        this.isUserMessage = isUserMessage;
        this.message = XssSanitizer.sanitize(message);
        this.dateOfCreation = dateOfCreation;
    }

    public void setMessage(String message) {
        this.message = XssSanitizer.sanitize(message);
    }
}
