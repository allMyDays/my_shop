package com.example.support_service.mapper;

import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.support_service.entity.SupportMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SupportMessageMapper {


    public abstract SupportMessageResponseDTO toSupportMessageDTO(SupportMessage supportMessage);

    public abstract List<SupportMessageResponseDTO> toSupportMessageDTOList(List<SupportMessage> supportMessages);
}
