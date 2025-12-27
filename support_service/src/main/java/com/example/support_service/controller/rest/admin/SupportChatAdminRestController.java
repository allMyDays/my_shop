package com.example.support_service.controller.rest.admin;

import com.example.common.dto.support.SupportChatResponseDTO;
import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.support_service.controller.rest.i.ISupportChatRestController;
import com.example.support_service.dto.CreateChatDto;
import com.example.support_service.mapper.SupportChatMapper;
import com.example.support_service.mapper.SupportMessageMapper;
import com.example.support_service.service.SupportUserService;
import com.example.support_service.service.admin.SupportAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support/admin/chat")
@PreAuthorize("hasAnyRole('AGENT','ADMIN')")
public class SupportChatAdminRestController{

    private final SupportAdminService supportAdminService;

    private final SupportMessageMapper supportMessageMapper;

    private final SupportChatMapper supportChatMapper;


    @GetMapping("/get_all")
    public List<SupportChatResponseDTO> getAllUserSupportChats(@RequestParam Long userId){
        return supportChatMapper.toSupportChatDTOList(supportAdminService.getAllUserSupportChats(userId));

    }

    @GetMapping("/get_active")
    public List<SupportChatResponseDTO> getAllActiveSupportChats(){
        return supportChatMapper.toSupportChatDTOList(supportAdminService.getAllActiveSupportChats());

    }

    @GetMapping("/count_active")
    public Integer countAllActiveSupportChats(){
        return supportAdminService.countAllActiveSupportChats();

    }













}
