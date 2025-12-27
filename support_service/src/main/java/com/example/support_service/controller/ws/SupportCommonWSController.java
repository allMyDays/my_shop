package com.example.support_service.controller.ws;
import com.example.common.exception.TooManyFunctionCallsException;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.support.SupportChatTypingStatusDTO;
import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.mapper.SupportMessageMapper;
import com.example.support_service.service.SupportUserService;
import com.example.support_service.service.admin.SupportAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Controller;

import java.util.Map;

import static com.example.common.service.CommonUserService.getMyUserEntityId;
import static com.example.common.service.CommonUserService.userIsAdminOrSupportAgent;

@Controller
@RequiredArgsConstructor
public class SupportCommonWSController {

    private final SupportUserService supportService;

    private final SimpMessagingTemplate messagingTemplate;

    private final JwtDecoder jwtDecoder;



    @MessageMapping("/handle_user_message")
    public void handleSupportUserMessage(SupportMessageResponseDTO messageDto, SimpMessageHeaderAccessor headerAccessor) throws UserNotFoundException {

            if(supportService.isMessageSendingLimited(messageDto.getChatId())){
                throw new TooManyFunctionCallsException();
            }

            messageDto.setUserMessage(true);

            messagingTemplate.convertAndSend("/support-chat-output-topic/" + messageDto.getChatId(), messageDto);

    }

    @MessageMapping("/handle_agent_message")
    public void handleSupportAgentMessage(SupportMessageResponseDTO messageDto, SimpMessageHeaderAccessor headerAccessor) throws UserNotFoundException {

        Jwt jwt = getAndValidateJwt(headerAccessor);

        if(!userIsAdminOrSupportAgent(jwt)){
            throw new RuntimeException("User with id %d do not have access to send agent messages".formatted(getMyUserEntityId(jwt)));
        }

        messageDto.setUserMessage(false);

        messagingTemplate.convertAndSend("/support-chat-output-topic/" + messageDto.getChatId(), messageDto);

    }


    @MessageMapping("/typing_status")
    public void handleTypingStatus(SupportChatTypingStatusDTO typingStatusDTO, SimpMessageHeaderAccessor headerAccessor) throws UserNotFoundException {

        messagingTemplate.convertAndSend("/support-chat-output-topic/"+typingStatusDTO.getChatId()+"/typing", typingStatusDTO);

    }





    private Jwt getAndValidateJwt(SimpMessageHeaderAccessor headerAccessor) throws UserNotFoundException {
        Map<String, Object> temp = headerAccessor.getSessionAttributes();
        if (temp==null) throw new UserNotFoundException();
        Jwt jwt = (Jwt)temp.get("jwt");
        if (jwt==null) throw new UserNotFoundException();
        jwtDecoder.decode(jwt.getTokenValue());
        return jwt;

    }




}
