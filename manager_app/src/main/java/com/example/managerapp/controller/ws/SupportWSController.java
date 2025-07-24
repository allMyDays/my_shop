package com.example.managerapp.controller.ws;

import com.example.managerapp.dto.SupportChatTypingStatusDTO;
import com.example.managerapp.dto.SupportMessageDTO;
import com.example.managerapp.entity.SupportMessage;
import com.example.managerapp.mapper.SupportMessageMapper;
import com.example.managerapp.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class SupportWSController {

    private final SupportService supportService;

    private final SupportMessageMapper supportMessageMapper;

    private final SimpMessagingTemplate messagingTemplate;



    @MessageMapping("/handle_user_message")
    @PreAuthorize("isAuthenticated()")
    public void handleSupportUserMessage(SupportMessageDTO payload, OAuth2AuthenticationToken authenticationToken){

        if(supportService.isSupportMessageCreationLimited(payload.getChatId())) return;

        Optional<SupportMessage> supportMessage =  supportService.saveMessage(authenticationToken, payload.getChatId(),payload.getMessage(),true);

        supportMessage.ifPresent(message ->
                messagingTemplate.convertAndSend("/topic/support_chat/" + payload.getChatId(), supportMessageMapper.toSupportMessageDTO(message)));


    }

    @MessageMapping("/handle_agent_message")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public void handleSupportAgentMessage(SupportMessageDTO payload, OAuth2AuthenticationToken authenticationToken){

        Optional<SupportMessage> supportMessage = supportService.saveMessage(authenticationToken,payload.getChatId(),payload.getMessage(),false);


        supportMessage.ifPresent(message -> messagingTemplate.convertAndSend("/topic/support_chat/" + payload.getChatId(), supportMessageMapper.toSupportMessageDTO(message)));

    }

    @MessageMapping("/typing_status")
    @PreAuthorize("isAuthenticated()")
    public void handleTypingStatus(SupportChatTypingStatusDTO typingStatusDTO){

        messagingTemplate.convertAndSend("/topic/support_chat/"+typingStatusDTO.getChatId()+"/typing", typingStatusDTO);

    }
}
