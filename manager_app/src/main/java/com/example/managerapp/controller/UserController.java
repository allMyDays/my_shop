
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
import org.springframework.mail.MailSendException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public String showRegistrationForm(Model model) {
        return "registration";
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
    @ResponseBody
    public Map<String, Object> editProfile(@Valid EditUserDTO userDTO,
                                           BindingResult res,
                                           @RequestParam boolean isVerified,
                                           OAuth2AuthenticationToken auth) {
        Map<String, Object> response = new HashMap<>();
        String userId = userService.getUserID(auth);

        if (res.hasErrors()) {
            response.put("errors", res.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
            return response;
        }

        List<String> errors = new ArrayList<>();

        if (!Objects.equals(userDTO.getPassword(), userDTO.getRepeatedPassword())) {
            errors.add("Пароли не совпадают.");
        }
        if (!isVerified&&userDTO.getEmail()!=null&&userService.UserExists(userDTO.getEmail(), null, userId) > 0) {
            errors.add("Пользователь с таким email уже существует.");
        }
        if (!errors.isEmpty()) {
            response.put("errors", errors);
            return response;
        }


        // email был изменён, но не подтверждён
        if (!isVerified&&userDTO.getEmail()!=null&&userService.isEmailChanged(auth, userDTO.getEmail())) {
            // отправим код
            try{
            emailService.sendRandomCodeToEmail(userDTO.getEmail());
            }catch (MailSendException e){}
            response.put("emailSent", true);
            return response;
        }

        userService.updateUserData(userId, userDTO);
        response.put("updated", true);
        return response;
    }





    /*  @PostMapping("/my_profile")
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

    }*/
    @PostMapping("/registration")
    @ResponseBody
    public Map<String, Object> register(@Validated RegistrationUserDTO userDTO,BindingResult bindingResult, @RequestParam boolean isVerified) {

        Map<String, Object> response = new HashMap<>();

        List<String> errors = new ArrayList<>();

        if(bindingResult.hasErrors()) {
            errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            response.put("errors", errors);
            return response;

        }

        if (!userDTO.getPassword().equals(userDTO.getRepeatedPassword())) {
            errors.add("Пароли не совпадают!");
        }

        int userExistsTemp = userService.UserExists(userDTO.getEmail(),userDTO.getNickName(),null);

        if (userExistsTemp > 0) {
            errors.add("Пользователь с таким %s уже существует".formatted(userExistsTemp==1?"email":"никнеймом"));
        }

        if (!errors.isEmpty()) {
            response.put("errors", errors);
            return response;
        }

        if (!isVerified) {
            try{
            emailService.sendRandomCodeToEmail(userDTO.getEmail());
            } catch (MailSendException e) {}
            response.put("emailSent", true);
            return response;
        }


        boolean success = userService.createUser(userDTO);
        response.put("userCreationSuccess", success);

        return response;
    }

}