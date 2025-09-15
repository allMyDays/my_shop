package com.example.support_service.controller.rest.admin;

import com.example.common.dto.support.SupportChatResponseDTO;
import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.support_service.mapper.SupportChatMapper;
import com.example.support_service.mapper.SupportMessageMapper;
import com.example.support_service.service.admin.SupportAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support/admin")
@PreAuthorize("hasAnyRole('AGENT','ADMIN')")
public class SupportAdminRestController {

    private final SupportAdminService supportAdminService;

    private final SupportMessageMapper supportMessageMapper;

    private final SupportChatMapper supportChatMapper;


    @GetMapping("/get_user_chats/{userId:\\d+}")
    public List<SupportChatResponseDTO> getAllUserSupportChats(@PathVariable Long userId){
        return supportChatMapper.toSupportChatDTOList(supportAdminService.getAllUserSupportChats(userId));

    }


    @GetMapping("/get_active_chats")
    public List<SupportChatResponseDTO> getAllActiveSupportChats(){
        return supportChatMapper.toSupportChatDTOList(supportAdminService.getAllActiveSupportChats());

    }

    @GetMapping("/get_chat_messages")
    public List<SupportMessageResponseDTO> getAllSupportChatMessages(@RequestParam Long chatId) {

        return supportMessageMapper.toSupportMessageDTOList(supportAdminService.getAllSupportChatMessages(chatId));

    }




}
