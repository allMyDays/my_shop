package com.example.managerapp.controller;

import com.example.managerapp.service.EmailService;
import com.example.managerapp.service.RedisService;
import com.example.managerapp.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class EmailController {

    private final UserService userService;
    private final RedisService redisService;


    @PostMapping("/verify_email")
    public String verifyEmail(@RequestParam String email, @RequestParam String code, @RequestParam Boolean isRegistrationPage, Model model) {

        String returningPage = isRegistrationPage?"registration":"profile";
        String expectedCode = redisService.get(email);

        if(expectedCode==null){
            model.addAttribute("EmailKeyNotExists", true);
            return returningPage;
        }
        if(!expectedCode.equals(code)){
            model.addAttribute("EmailKeyNotEquals", true);
            return returningPage;
        }
        userService.markEmailAsVerified(email);
        model.addAttribute("EmailSuccess", true);
        return returningPage;

    }






}
