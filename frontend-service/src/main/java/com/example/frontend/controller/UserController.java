package com.example.frontend.controller;


import com.example.common.client.grpc.OrderGrpcClient;
import com.example.common.client.grpc.UserGrpcClient;
import com.example.common.dto.user.rest.UserAddressDto;
import com.example.common.dto.user.rest.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.example.common.service.CommonUserService.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserGrpcClient userGrpcClient;

    private final OrderGrpcClient orderGrpcClient;

    private final Environment environment;

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("isTestMode", environment.acceptsProfiles(Profiles.of("test")));
        return "registration";
    }

    @GetMapping("/welcome")
    public String showLoginForm(Model model) {
        model.addAttribute("isTestMode", environment.acceptsProfiles(Profiles.of("test")));
        return "my_login";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model, @AuthenticationPrincipal Jwt jwt){
        if(jwt == null){
            return "redirect:/welcome";
        }
        long userId = getMyUserEntityId(jwt);
        UserResponseDTO userResponseDTO =  userGrpcClient.getUserInfoByEntityId(userId);
        Optional<UserAddressDto> addressDto = orderGrpcClient.getUserAddress(userId);
        addressDto.ifPresent(userAddressDto -> userResponseDTO.setFullAddress(userAddressDto.getFullAddress()));

        model.addAttribute("user", userResponseDTO);

        model.addAttribute("isUserStaff", userIsAdminOrSupportAgent(jwt));
        model.addAttribute("currentUserId", userId);
        model.addAttribute("isTestMode", environment.acceptsProfiles(Profiles.of("test")));
        return "profile";
    }

    @GetMapping("/reset_password")
    public String reset_password(Model model, @AuthenticationPrincipal Jwt jwt) {
       if(jwt!=null){
           model.addAttribute("isUserStaff", userIsAdminOrSupportAgent(jwt));
           model.addAttribute("currentUserId", getMyUserEntityId(jwt));
       }
        model.addAttribute("isTestMode", environment.acceptsProfiles(Profiles.of("test")));
        return "password_reset";
    }












}
