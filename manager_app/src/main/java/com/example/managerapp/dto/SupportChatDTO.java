package com.example.managerapp.dto;

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

    List<SupportMessageDTO> messages = new ArrayList<>();








}
