package com.example.frontend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    @GetMapping("/all")
    public String OrdersPage(Model model, @AuthenticationPrincipal Jwt jwt) {
        return "orders";
    }




}
