
package com.example.managerapp.controller;


import com.example.managerapp.dto.EditUserDTO;
import com.example.managerapp.dto.GetUserDTO;
import com.example.managerapp.dto.RegistrationUserDTO;
import com.example.managerapp.service.EmailService;
import com.example.managerapp.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.prepost.PreAuthorize;
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


    @InitBinder
    public void initBinder(WebDataBinder binder) {  // для того чтобы все пустые поля приходили как null
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/registration")
    @PreAuthorize("!isAuthenticated()")
    public String showRegistrationForm() {
        return "registration";
    }


    @GetMapping("/my_profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model, OAuth2AuthenticationToken authentication) {
        Optional<GetUserDTO> userOptional =  userService.collectUserInfo(authentication);
        if(userOptional.isEmpty()) return "redirect:/login";
        model.addAttribute("user", userOptional.get());
        return "profile";

    }

    @PostMapping("/my_profile")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> editProfile(@Valid EditUserDTO userDTO,
                                           BindingResult res,
                                           @RequestParam boolean isVerified,
                                           OAuth2AuthenticationToken auth) {
        Map<String, Object> response = new HashMap<>();
        String userId = userService.getUserKeycloakID(auth);

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
        if (!isVerified&&userDTO.getEmail()!=null&&userService.isUserEmailChanged(auth, userDTO.getEmail())) {
            // отправим код
            try{
            emailService.sendRandomCodeToEmail(userDTO.getEmail());
            }catch (MailSendException e){}
            response.put("emailSent", true);
            return response;
        }

        userService.updateUserData(userId, userDTO);
        response.put("userSuccess", true);
        return response;
    }



    @PostMapping("/registration")
    @ResponseBody
    @PreAuthorize("!isAuthenticated()")
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
        response.put("userSuccess", success);

        return response;
    }

    @GetMapping("/get_user_roles")
    @ResponseBody
    public ResponseEntity<List<String>> getUserRoles(OAuth2AuthenticationToken authentication) {

        Optional<List<String>> optionalList = userService.getUserRoles(authentication);

        return optionalList.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity
                .badRequest()
                .build());

    }





}