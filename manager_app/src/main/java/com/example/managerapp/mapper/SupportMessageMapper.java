package com.example.managerapp.mapper;

import com.example.managerapp.dto.support.SupportMessageDTO;
import com.example.managerapp.entity.SupportMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SupportMessageMapper {


    @Mapping(target = "chatId", expression = "java(supportMessage.getChat().getId())")
    public abstract SupportMessageDTO toSupportMessageDTO(SupportMessage supportMessage);

    public abstract List<SupportMessageDTO> toSupportMessageDTOList(List<SupportMessage> supportMessages);
}
