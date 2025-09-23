package com.example.frontend.controller;


import com.example.common.client.grpc.UserGrpcClient;
import com.example.common.dto.user.UserResponseDTO;
import com.example.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.example.common.service.CommonUserService.getUserKeycloakId;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserGrpcClient userGrpcClient;

    @GetMapping("/registration")
    @PreAuthorize("!isAuthenticated()")
    public String showRegistrationForm(Model model) {
        return "registration";
    }

    @GetMapping("/welcome")
    @PreAuthorize("!isAuthenticated()")
    public String showLoginForm() {
        return "my_login";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model, @AuthenticationPrincipal Jwt jwt){
        UserResponseDTO userResponseDTO =  userGrpcClient.getUserInfo(getUserKeycloakId(jwt));
        model.addAttribute("user", userResponseDTO);
        return "profile";
    }

    @GetMapping("/reset_password")
    @PreAuthorize("!isAuthenticated()")
    public String reset_password(Model model) {
        return "password_reset";
    }












}
