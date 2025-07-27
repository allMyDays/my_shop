package com.example.managerapp.dto.support;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SupportChatDTO {

    private Long id;

    private LocalDateTime dateOfCreation;

    private String topic;

    private boolean closed;

    private boolean needsAnswer;

    private Long userId;

    List<SupportMessageDTO> messages = new ArrayList<>();








}
