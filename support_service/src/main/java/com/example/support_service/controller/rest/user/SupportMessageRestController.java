package com.example.support_service.controller.rest.user;

import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.support_service.controller.rest.i.ISupportMessageRestController;
import com.example.support_service.dto.CreateSupportMessageRequestDto;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.mapper.SupportMessageMapper;
import com.example.support_service.service.SupportUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support/message")
@PreAuthorize("isAuthenticated()")
public class SupportMessageRestController implements ISupportMessageRestController {

    private final SupportUserService supportUserService;

    private final SupportMessageMapper supportMessageMapper;

    @GetMapping("/get")
    public SupportMessageResponseDTO getOneChatMessage(@RequestParam Long messageId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        return supportMessageMapper.toSupportMessageDTO(supportUserService.getOneSupportChatMessage(getMyUserEntityId(jwt), messageId));

    }


    @GetMapping("/get_all")
    public List<SupportMessageResponseDTO> getAllChatMessages(@RequestParam Long chatId, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        return supportMessageMapper.toSupportMessageDTOList(supportUserService.getAllSupportChatMessages(getMyUserEntityId(jwt),chatId));

    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createMessage(
            @Validated @ModelAttribute CreateSupportMessageRequestDto createMessageDto,
            BindingResult bindingResult,
            @RequestPart(name = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        if(supportUserService.isMessageSendingLimited(createMessageDto.getChatId())){
            return ResponseEntity
                    .status(429)
                    .body("You temporarily exhausted the limit of sending support messages");
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(bindingResult.getAllErrors().stream()
                       .map(DefaultMessageSourceResolvable::getDefaultMessage)
                       .toList());
        }

        SupportMessageResponseDTO messageResponseDTO = supportMessageMapper.toSupportMessageDTO(
                supportUserService.saveSupportMessage(getMyUserEntityId(jwt), createMessageDto.getChatId(), createMessageDto.getMessage(),images)
        );
        messageResponseDTO.setChatId(createMessageDto.getChatId());
        return ResponseEntity.ok(messageResponseDTO);

    }

}
