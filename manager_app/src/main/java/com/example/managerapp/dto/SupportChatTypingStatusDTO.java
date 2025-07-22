package com.example.managerapp.dto;

import lombok.Data;
@Data
public class SupportChatTypingStatusDTO {

    Long chatId;

    boolean typing;

    boolean agent;

}
