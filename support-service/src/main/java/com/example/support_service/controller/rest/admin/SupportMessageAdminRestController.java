package com.example.support_service.controller.rest.admin;

import com.example.common.dto.support.SupportChatResponseDTO;
import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.support_service.controller.rest.i.ISupportChatRestController;
import com.example.support_service.dto.CreateChatDto;
import com.example.support_service.dto.CreateSupportMessageRequestDto;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support/admin/message")
@PreAuthorize("hasAnyRole('AGENT','ADMIN')")
public class SupportMessageAdminRestController{

    private final SupportAdminService supportAdminService;

    private final SupportMessageMapper supportMessageMapper;


    @GetMapping("/get")
    public SupportMessageResponseDTO getOneChatMessage(@RequestParam Long messageId) throws UserNotFoundException {

        return supportMessageMapper.toSupportMessageDTO(supportAdminService.getOneSupportChatMessage(messageId));

    }


    @GetMapping("/get_all")
    public List<SupportMessageResponseDTO> getAllChatMessages(@RequestParam Long chatId) {

        return supportMessageMapper.toSupportMessageDTOList(supportAdminService.getAllSupportChatMessages(chatId));

    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createMessage(
            @Validated @ModelAttribute CreateSupportMessageRequestDto createMessageDto,
            BindingResult bindingResult,
            @RequestPart(name = "images", required = false) List<MultipartFile> images) throws UserNotFoundException {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(bindingResult.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .toList());
        }

        SupportMessageResponseDTO messageResponseDTO = supportMessageMapper.toSupportMessageDTO(
                supportAdminService.saveSupportMessage(createMessageDto.getChatId(), createMessageDto.getMessage(),images)
        );
        messageResponseDTO.setChatId(createMessageDto.getChatId());
        return ResponseEntity.ok(messageResponseDTO);

    }

















}
