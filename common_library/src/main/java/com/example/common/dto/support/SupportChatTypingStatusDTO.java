package com.example.common.dto.support;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SupportChatTypingStatusDTO {

    Long chatId;

    boolean typing;

    boolean agent;

}
