package com.example.support_service.controller.rest.user;

import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.support_service.mapper.SupportMessageMapper;
import com.example.support_service.service.SupportUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support/message")
@PreAuthorize("isAuthenticated()")
public class SupportMessageRestController {

    private final SupportUserService supportUserService;

    private final SupportMessageMapper supportMessageMapper;

    @GetMapping("/send-ability")
    public ResponseEntity<?> supportMessageCheckSendingLimit(@RequestParam Long chatId){
        if(supportUserService.supportMessageCreationIsLimited(chatId)){
            return ResponseEntity
                    .status(429)
                    .build();
        }

        return ResponseEntity
                .ok()
                .build();

    }

    @GetMapping("/get_all")
    public List<SupportMessageResponseDTO> getAllSupportChatMessages(@AuthenticationPrincipal Jwt jwt, @RequestParam Long chatId) throws UserNotFoundException {

        return supportMessageMapper.toSupportMessageDTOList(supportUserService.getAllSupportChatMessages(getMyUserEntityId(jwt),chatId));

    }











}
