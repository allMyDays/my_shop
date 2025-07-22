package com.example.managerapp.controller.rest;


import com.example.managerapp.dto.SupportChatDTO;
import com.example.managerapp.dto.SupportChatTypingStatusDTO;
import com.example.managerapp.dto.SupportMessageDTO;
import com.example.managerapp.entity.SupportMessage;
import com.example.managerapp.mapper.SupportChatMapper;
import com.example.managerapp.mapper.SupportMessageMapper;
import com.example.managerapp.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;

    private final SimpMessagingTemplate messagingTemplate;

    private final SupportMessageMapper supportMessageMapper;

    private final SupportChatMapper supportChatMapper;


    @MessageMapping("/handle_user_message")
    public void handleSupportUserMessage(SupportMessageDTO payload){

        SupportMessage supportMessage =  supportService.saveMessage(payload.getChatId(),payload.getMessage(),true);

        // переслать сообщение агенту
        messagingTemplate.convertAndSend("/topic/support_chat/"+payload.getChatId(), supportMessageMapper.toSupportMessageDTO(supportMessage) );

    }

    @MessageMapping("/typing_status")
    public void handleTypingStatus(SupportChatTypingStatusDTO typingStatusDTO){

        messagingTemplate.convertAndSend("/topic/support_chat/"+typingStatusDTO.getChatId()+"/typing", typingStatusDTO);

    }


    @GetMapping("/handle_message_check_limit")
    public ResponseEntity<?> handleMessageCheckLimit(OAuth2AuthenticationToken authentication, @RequestParam Long chatId){
        if(supportService.isSupportMessageCreationLimited(chatId)){
            return ResponseEntity
                    .status(429)
                    .build();
        }

        return ResponseEntity
                .ok()
                .build();

    }

    @GetMapping("/create_chat")
    public ResponseEntity<?> createChat(OAuth2AuthenticationToken authentication, @RequestParam String topic){
        Map<String, Long> result = supportService.createSupportChat(authentication, topic);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);

    }

    @GetMapping("/create_chat_check_limit")
    public ResponseEntity<?> createChatCheckLimit(OAuth2AuthenticationToken authentication){
           if(supportService.isSupportChatCreationLimited(authentication)){
              return ResponseEntity
                      .status(429)
                      .build();
          }

          return ResponseEntity
                 .ok()
                 .build();

    }

    @GetMapping("/get_user_chats")
    public List<SupportChatDTO> getAllUserChats(OAuth2AuthenticationToken authentication, @RequestParam(required = false) Long userId){
        return supportChatMapper.toSupportChatDTOList(supportService.getAllUserSupportChats(authentication,userId));

    }

    @GetMapping("/get_chat_messages")
    public List<SupportMessageDTO> getAllChatMessages(OAuth2AuthenticationToken authentication, @RequestParam Long chatId){

        return supportMessageMapper.toSupportMessageDTOList(supportService.getAllChatMessages(authentication,chatId));


    }

    @DeleteMapping("/delete_chat")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> deleteChat(OAuth2AuthenticationToken authentication, @RequestParam Long chatId){

        if(supportService.deleteSupportChat(authentication,chatId)){
            return ResponseEntity
                    .ok()
                    .build();
        }

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();

    }






    @GetMapping("/get_active_chats")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public List<SupportChatDTO> getAllActiveChats(OAuth2AuthenticationToken authentication){
        return supportChatMapper.toSupportChatDTOList(supportService.getAllActiveChats(authentication));

    }

    @MessageMapping("/handle_agent_message")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public void handleSupportAgentMessage(SupportMessageDTO payload){

        SupportMessage supportMessage = supportService.saveMessage(payload.getChatId(),payload.getMessage(),false);

        // переслать сообщение юзеру
        messagingTemplate.convertAndSend("/topic/support_chat/"+payload.getChatId(), supportMessageMapper.toSupportMessageDTO(supportMessage));

    }




}
