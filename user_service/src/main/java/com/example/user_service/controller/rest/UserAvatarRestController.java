package com.example.user_service.controller.rest;

import com.example.common.exception.UserNotFoundException;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/avatar")
@PreAuthorize("isAuthenticated()")
public class UserAvatarRestController {

    private final UserService userService;


    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> uploadUserAvatar(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        userService.sendUploadUserAvatarRequest(file, getMyUserEntityId(jwt));

        return ResponseEntity.ok()
                .build();

    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUserAvatar(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

       userService.deleteUserAvatar(getMyUserEntityId(jwt));

        return ResponseEntity.ok()
                .build();

    }







}
