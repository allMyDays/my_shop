
package com.example.managerapp.controller;


import com.example.managerapp.dto.EditUserDTO;
import com.example.managerapp.dto.RegistrationUserDTO;
import com.example.managerapp.entity.MyUser;
import com.example.managerapp.mapper.UserMapper;
import com.example.managerapp.service.EmailService;
import com.example.managerapp.service.RedisService;
import com.example.managerapp.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class UserController {

    private final EmailService emailService;
    private UserService userService;

    private UserMapper userMapper;

    private RedisService redisService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {  // для того чтобы все пустые поля приходили как null
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/registration")
    public String showForm(Model model) {
        model.addAttribute("newUser", new RegistrationUserDTO());
        return "registration";
    }

    @PostMapping("/registration")
    public String createUser(@Valid RegistrationUserDTO userDTO, BindingResult res,  Model model) {
        model.addAttribute("newUser", userDTO);

        if(res.hasErrors()) {
            model.addAttribute("errors", res.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
            return "registration";
        }
        if(!userDTO.getPassword().equals(userDTO.getRepeatedPassword())){
            model.addAttribute("notMatchingPasswords",true);
            return "registration";
        }
        int userExistsTemp = userService.UserExists(userDTO.getEmail(),userDTO.getNickName(),null);
        if(userExistsTemp>0){
            model.addAttribute("userExists", userExistsTemp);
            return "registration";
        }

        if(redisService.get(userDTO.getEmail())==null){
            emailService.sendRandomCodeToEmail(userDTO.getEmail());
            return "registration";
        }




        if(!userService.createUser(userDTO)){
            model.addAttribute("notCreated", true);
            return "registration";
        }
        return "redirect:/login";

    }

    @GetMapping("/my_profile")
    public String profile(Model model, OAuth2AuthenticationToken authentication) {
        Optional<MyUser> userOptional =  userService.collectMinimumUserInfo(authentication);
        if(userOptional.isEmpty()) return "redirect:/login";

        EditUserDTO userDTO = userMapper.toEditUserDTO(userOptional.get());

        model.addAttribute("user", userDTO);
        return "profile";

    }

    @PostMapping("/my_profile")
    public String profileEdit(@Valid EditUserDTO userDTO, BindingResult res, Model model, OAuth2AuthenticationToken authentication) {
        if (authentication == null) return "redirect:/login";

        model.addAttribute("user", userDTO);

        if(res.hasErrors()) {
            model.addAttribute("errors", res.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
            return "profile";
        }
        if(!Objects.equals(userDTO.getRepeatedPassword(),userDTO.getPassword())){
            model.addAttribute("notMatchingPasswords",true);
            return "profile";
        }

        int userExistsTemp = userService.UserExists(userDTO.getEmail(),null,userService.getUserID(authentication));

        if(userExistsTemp>0){
            model.addAttribute("userExists", userExistsTemp);
            return "profile";
        }
        userService.updateUserData(userService.getUserID(authentication),userDTO);

        model.addAttribute("updated", true);
        return "profile";


    }

}