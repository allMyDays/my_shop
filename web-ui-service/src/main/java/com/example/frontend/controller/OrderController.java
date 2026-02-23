package com.example.frontend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.example.common.service.CommonUserService.getMyUserEntityId;
import static com.example.common.service.CommonUserService.userIsAdminOrSupportAgent;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final Environment environment;

    @GetMapping("/all")
    public String OrdersPage(Model model, @AuthenticationPrincipal Jwt jwt) {
        if(jwt == null){
            return "redirect:/welcome";
        }
        model.addAttribute("currentUserId",getMyUserEntityId(jwt));
        model.addAttribute("isUserStaff", userIsAdminOrSupportAgent(jwt));
        model.addAttribute("isTestMode", environment.acceptsProfiles(Profiles.of("test")));
        return "orders";
    }




}
