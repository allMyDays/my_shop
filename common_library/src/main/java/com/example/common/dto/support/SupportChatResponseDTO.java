package com.example.common.dto.support;


import com.example.common.security.XssSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Schema(description = "Информация о чате поддержки")
public class SupportChatResponseDTO {

    @Setter
    @Schema(description = "ID чата", example = "123")
    private Long id;

    @Setter
    @Schema(description = "Дата создания чата")
    private LocalDateTime dateOfCreation;

    @Schema(description = "Тема чата", example = "Проблема с оплатой")
    private String topic;

    @Setter
    @Schema(description = "Закрыт ли чат навсегда", example = "true")
    private boolean closed;

    @Setter
    @Schema(description = "Нуждается ли пользователь в ответе в этом чате", example = "true")
    private boolean needsAnswer;

    @Setter
    @Schema(description = "ID пользователя, создавшего этот чат", example = "1234")
    private Long userId;

    @Setter
    @Schema(description = "Содержит ли данный чат хоть какие-то сообщения", example = "true")
    boolean containsMessages;

    @Setter
    @Schema(description = "Сообщения чата")
    List<SupportMessageResponseDTO> messages = new ArrayList<>();


    public SupportChatResponseDTO(Long id, LocalDateTime dateOfCreation, String topic, boolean closed, boolean needsAnswer, Long userId, List<SupportMessageResponseDTO> messages) {
        this.id = id;
        this.dateOfCreation = dateOfCreation;
        this.topic = XssSanitizer.sanitize(topic);
        this.closed = closed;
        this.needsAnswer = needsAnswer;
        this.userId = userId;
        this.messages = messages;
    }

    public void setTopic(String topic) {
        this.topic = XssSanitizer.sanitize(topic);
    }
}
