package com.example.support_service.mapper;

import com.example.common.dto.support.SupportChatResponseDTO;
import com.example.support_service.entity.SupportChat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring", uses = SupportMessageMapper.class)
public abstract class SupportChatMapper {

    @Mapping(target = "closed",expression = "java(chatClosed(supportChat.getDateOfCreation()))")
    public abstract SupportChatResponseDTO toSupportChatDTO(SupportChat supportChat);

    public abstract List<SupportChatResponseDTO> toSupportChatDTOList(List<SupportChat> supportChats);

    public boolean chatClosed(LocalDateTime dateOfCreation) {
        return ChronoUnit.DAYS.between(dateOfCreation, LocalDateTime.now()) >= 7;
    }


}
