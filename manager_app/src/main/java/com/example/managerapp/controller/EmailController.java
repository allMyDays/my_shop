package com.example.managerapp.controller;

import com.example.managerapp.service.EmailService;
import com.example.managerapp.service.RedisService;
import com.example.managerapp.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@AllArgsConstructor
public class EmailController {

    private final UserService userService;
    private final RedisService redisService;


    @PostMapping(value = "/verify_email", produces = "application/json")
    @ResponseBody
    public Map<String, Object> verify(@RequestParam String email,
                                      @RequestParam String code) {
        Map<String, Object> res = new HashMap<>();

        String expected = redisService.get(email);
        if (expected == null) {
            res.put("expired", true);
        } else if (!expected.equals(code)) {
            res.put("notMatch", true);
        } else {
            res.put("success", true);
        }

        return res;
    }






}
