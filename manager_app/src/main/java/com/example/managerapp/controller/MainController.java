package com.example.managerapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(){
        return "index";


    }
    @GetMapping("/login")
    public String login(){
        return "login";


    }

    @GetMapping("/login-error")
    public String login_error(Model model){
        model.addAttribute("loginError", true);
        return "login";

    }



}
