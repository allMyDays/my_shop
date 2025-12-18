package com.example.frontend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.example.common.service.CommonUserService.getMyUserEntityId;
import static com.example.common.service.CommonUserService.userIsAdminOrSupportAgent;


@Controller
@RequiredArgsConstructor
public class MainController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal Jwt jwt){
        if(jwt!=null){
            model.addAttribute("currentUserId",getMyUserEntityId(jwt));
            model.addAttribute("isUserStaff", userIsAdminOrSupportAgent(jwt));
        }
        return "index";
    }



}
