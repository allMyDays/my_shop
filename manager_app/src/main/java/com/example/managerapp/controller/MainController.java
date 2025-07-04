package com.example.managerapp.controller;

import com.example.managerapp.service.WishListService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class MainController {



    @GetMapping("/")
    public String index(){

        return "index";


    }
    @GetMapping("/login")
    public String login(){
        return "redirect:/oauth2/authorization/keycloak";


    }

    @GetMapping("/login-error")
    public String login_error(Model model){
        model.addAttribute("loginError", true);
        return "login";

    }

    @GetMapping("/my_logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:http://localhost:8084/realms/my_realm/protocol/openid-connect/logout?redirect_uri=http://localhost:8083/";
    }



}
