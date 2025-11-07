package com.example.common.dto.support;


import com.example.common.security.XssSanitizer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class SupportChatResponseDTO {

    @Setter
    private Long id;

    @Setter
    private LocalDateTime dateOfCreation;

    private String topic;

    @Setter
    private boolean closed;

    @Setter
    private boolean needsAnswer;

    @Setter
    private Long userId;

    @Setter
    boolean containsMessages;

    @Setter
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
