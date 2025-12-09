package com.example.common.dto.support;


import com.example.common.security.XssSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Schema(description = "Сообщение в чате поддержки")
public class SupportMessageResponseDTO {


    @Setter
    @Schema(description = "ID чата", example = "123")
    private Long chatId;

    @Setter
    @Schema(description = "Пренадлежит ли сообщение пользователю, создавшего чат", example = "true")
    private boolean isUserMessage;

    @Schema(description = "Текстовое сообщение", example = "Здравствуйте!")
    private String message;

    @Setter
    @Schema(description = "Дата создания (отправки) сообщения")
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
