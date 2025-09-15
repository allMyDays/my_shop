package com.example.support_service.controller.ws;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.support.SupportChatTypingStatusDTO;
import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.mapper.SupportMessageMapper;
import com.example.support_service.service.SupportUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;

import static com.example.common.service.CommonUserService.getMyUserEntityId;
import static com.example.common.service.CommonUserService.userIsAdminOrSupportAgent;

@Controller
@RequiredArgsConstructor
public class SupportCommonWSController {

    private final SupportUserService supportService;

    private final SupportMessageMapper supportMessageMapper;

    private final SimpMessagingTemplate messagingTemplate;



    @MessageMapping("/handle_user_message")
    public void handleSupportUserMessage(SupportMessageResponseDTO dto, SimpMessageHeaderAccessor headerAccessor) throws UserNotFoundException {

            Long userId = getMyUserEntityId(getJwt(headerAccessor));

            if(supportService.supportMessageCreationIsLimited(dto.getChatId())) return;

            Optional<SupportMessage> supportMessage =  supportService.saveSupportMessage(userId, dto.getChatId(),dto.getMessage(),true);

            supportMessage.ifPresent(message ->
                   messagingTemplate.convertAndSend("/support-chat-output-topic/" + dto.getChatId(), supportMessageMapper.toSupportMessageDTO(message)));


    }

    @MessageMapping("/handle_agent_message")
    public void handleSupportAgentMessage(SupportMessageResponseDTO payload, SimpMessageHeaderAccessor headerAccessor) throws UserNotFoundException {

        if(!userIsAdminOrSupportAgent(getJwt(headerAccessor))) return;

        Long userId = getMyUserEntityId(getJwt(headerAccessor));

        Optional<SupportMessage> supportMessage = supportService.saveSupportMessage(userId,payload.getChatId(),payload.getMessage(),false);

        supportMessage.ifPresent(message -> messagingTemplate.convertAndSend("/support-chat-output-topic/" + payload.getChatId(), supportMessageMapper.toSupportMessageDTO(message)));

    }


    @MessageMapping("/typing_status")
    public void handleTypingStatus(SupportChatTypingStatusDTO typingStatusDTO, SimpMessageHeaderAccessor headerAccessor) throws UserNotFoundException {

        if(typingStatusDTO.isAgent()&&!userIsAdminOrSupportAgent(getJwt(headerAccessor))) return;

        messagingTemplate.convertAndSend("/support-chat-output-topic/"+typingStatusDTO.getChatId()+"/typing", typingStatusDTO);

    }

    private Jwt getJwt(SimpMessageHeaderAccessor headerAccessor) throws UserNotFoundException {
        Map<String, Object> temp = headerAccessor.getSessionAttributes();
        if (temp==null) throw new UserNotFoundException();
        Jwt jwt = (Jwt)temp.get("jwt");
        if (jwt==null) throw new UserNotFoundException();
        return jwt;

    }




}
