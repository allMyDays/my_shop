package com.example.frontend.controller;

import com.example.common.client.grpc.UserGrpcClient;
import com.example.common.dto.user.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN','AGENT')")
@RequiredArgsConstructor
public class AdminController {

    private final UserGrpcClient userGrpcClient;

    @GetMapping("/profile")
    public String profile(Model model, @RequestParam Long userId){
        UserResponseDTO userResponseDTO =  userGrpcClient.getUserInfo2(userId);
        model.addAttribute("user", userResponseDTO);
        return "profile_admin_page";
    }










}
