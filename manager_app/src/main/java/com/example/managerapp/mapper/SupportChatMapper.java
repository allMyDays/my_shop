package com.example.managerapp.mapper;

import com.example.managerapp.dto.SupportChatDTO;
import com.example.managerapp.entity.SupportChat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring", uses = SupportMessageMapper.class)
public abstract class SupportChatMapper {

    @Mapping(target = "closed",expression = "java(chatClosed(supportChat.getDateOfCreation()))")
    public abstract SupportChatDTO toSupportChatDTO(SupportChat supportChat);

    public abstract List<SupportChatDTO> toSupportChatDTOList(List<SupportChat> supportChats);

    public boolean chatClosed(LocalDateTime dateOfCreation) {
        return ChronoUnit.DAYS.between(dateOfCreation, LocalDateTime.now()) >= 7;
    }


}
