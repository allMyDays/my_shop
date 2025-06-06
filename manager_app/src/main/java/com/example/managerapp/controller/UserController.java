
package com.example.managerapp.controller;


import com.example.managerapp.record.MyUser;
import com.example.managerapp.rest.UserRestClient;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class UserController {
  //  private final UserRestClient userRestClient;

  //  @Autowired
 //   public UserController(UserRestClient userRestClient) {
  //      this.userRestClient = userRestClient;
 //   }

    @GetMapping("/new_user")
    public String newUser(Model model) {
        model.addAttribute("user", new MyUser());
        return "auth";
    }

    @PostMapping("/new_user")
    public String newUser(BindingResult res, @Valid MyUser userDTO, Model model) {

        if(res.hasErrors()) {
            model.addAttribute("user", userDTO);
            model.addAttribute("errors", res.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList()); //todo
            return "auth";
        }


   /*     if(!userRestClient.createUser(userDTO)){
            model.addAttribute("emailExists", true);
            return "auth";
        }*/
        return "redirect:/login";

    }

    @GetMapping("/my_profile")
    public String profile(Model model, Principal principal) {
     /*   if (principal == null) return "redirect:/login";
        MyUser user =  (MyUser)userService.loadUserByUsername(principal.getName());
        MyUser userDTO = userMapper.toUserDTO(user);
        model.addAttribute("user", userDTO);*/
        return "profile";

    }

    @PostMapping("/my_profile")
    public String profileEdit(Model model, Principal principal, MyUser userDTO) {
        if (principal == null) return "redirect:/login";
//        userRestClient.updateProfile(userDTO, principal);
        //todo поменять емайл в принсипал
        return "redirect:/";

    }





}