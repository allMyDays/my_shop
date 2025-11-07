package com.example.support_service.controller.rest.user;

import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.support.SupportChatResponseDTO;
import com.example.support_service.dto.CreateChatDto;
import com.example.support_service.mapper.SupportChatMapper;
import com.example.support_service.service.SupportUserService;
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
@RequestMapping("/api/support/chat")
@PreAuthorize("isAuthenticated()")
public class SupportChatRestController {

    private final SupportUserService supportUserService;

    private final SupportChatMapper supportChatMapper;

    @PostMapping("/create")
    public ResponseEntity<?> createSupportChat(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateChatDto createChatDto, BindingResult bindingResult) throws UserNotFoundException {

        if(bindingResult.hasErrors()) {

            return ResponseEntity
                    .badRequest()
                    .body(bindingResult.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .toList());
        }

        Long userId = getMyUserEntityId(jwt);

        Long newChatId = supportUserService.createSupportChat(userId, createChatDto.getTopic());

        System.out.println("!!!!!!!!!!chat was created with id: " + newChatId);

        Map<String, Long> result = new HashMap<>();
        result.put("chatId", newChatId);
        result.put("userId", userId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);

    }
    @GetMapping("/create-ability")
    public ResponseEntity<?> checkChatCreationAbility(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        if(supportUserService.chatCreationIsLimited(getMyUserEntityId(jwt))){
            return ResponseEntity
                    .status(429)
                    .body("You temporarily exhausted the limit of creation support chats");
        }

        return ResponseEntity
                .ok()
                .build();

    }
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteSupportChat(@AuthenticationPrincipal Jwt jwt, @RequestParam Long chatId) throws UserNotFoundException {

        supportUserService.deleteSupportChat(getMyUserEntityId(jwt),chatId);

            return ResponseEntity
                    .ok()
                    .build();


    }

    @GetMapping("/get_all")
    public List<SupportChatResponseDTO> getAllUserSupportChats(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        return supportChatMapper.toSupportChatDTOList(supportUserService.getAllUserSupportChats(getMyUserEntityId(jwt)));

    }













}
