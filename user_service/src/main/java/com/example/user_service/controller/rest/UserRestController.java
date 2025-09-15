package com.example.user_service.controller.rest;

import com.example.common.dto.user.UpdateUserRequestDTO;
import com.example.common.dto.user.CreateUserRequestDTO;
import com.example.common.dto.user.UserResponseDTO;
import com.example.common.enumeration.grpc.UserExistenceStatus;
import com.example.common.exception.UserNotFoundException;
import com.example.common.service.CommonUserService;
import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.VerifyEmailRequestDTO;
import com.example.user_service.service.RedisService;
import com.example.user_service.service.UserKeycloakService;
import com.example.user_service.service.UserService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.common.enumeration.grpc.UserExistenceStatus.EMAIL_EXISTS;
import static com.example.common.enumeration.grpc.UserExistenceStatus.NOT_EXISTS;
import static com.example.common.service.CommonUserService.getMyUserEntityId;
import static com.example.common.service.CommonUserService.getUserKeycloakId;
import static com.example.user_service.enumeration.RedisSubKeys.CONFIRMED_EMAIL;
import static com.example.user_service.enumeration.RedisSubKeys.CONFIRMING_EMAIL;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserRestController {

    private final UserService userService;

    private final UserKeycloakService userKeycloakService;

    private final RedisService redisService;




    @InitBinder
    public void initBinder(WebDataBinder binder) {  // для того чтобы все пустые поля приходили как null
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/get_roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getUserRoles(@AuthenticationPrincipal Jwt jwt) {

        Optional<List<String>> optionalList = CommonUserService.getUserRoles(jwt);

        return optionalList.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity
                .badRequest()
                .build());

    }

    @PostMapping("/create")
    @PreAuthorize("!isAuthenticated()")
    public Map<String, Object> createUser(@Validated @RequestBody CreateUserRequestDTO userDTO, BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        if(bindingResult.hasErrors()) {
            response.put("errors", bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
            return response;

        }
        UserExistenceStatus existenceStatus = userKeycloakService.userExists(userDTO.getEmail(),userDTO.getNickName(),null);

        if (!existenceStatus.equals(NOT_EXISTS)) {
            response.put("errors", List.of("Пользователь с таким %s уже существует".formatted(existenceStatus.equals(EMAIL_EXISTS)?"email":"никнеймом")));
            return response;
        }

        if (!userService.userEmailIsVerifiedOrSendCodeOtherwise(userDTO.getEmail())) {
            response.put("emailSent", true);
            return response;
        }
        boolean success = userService.createCommonUser(userDTO);
        response.put("userSuccess", success);

        return response;

    }
   @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> updateUser(@Validated @RequestBody UpdateUserRequestDTO userDTO, BindingResult res, @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> response = new HashMap<>();

       if (res.hasErrors()) {
            response.put("errors", res.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
            return response;
        }

        if(userDTO.getEmail()!=null){
            if(userKeycloakService.userEmailIsChanged(getUserKeycloakId(jwt), userDTO.getEmail())){
                if(userKeycloakService.userExists(userDTO.getEmail(), null, getUserKeycloakId(jwt)).equals(EMAIL_EXISTS)){
                    response.put("errors", List.of("Пользователь с таким email уже существует."));
                    return response;
                }
                if(!userService.userEmailIsVerifiedOrSendCodeOtherwise(userDTO.getEmail())){
                    response.put("emailSent", true);
                    return response;
                }
            } else{
                userDTO.setEmail(null);
            }
        }

       boolean success = userKeycloakService.updateUserData(getUserKeycloakId(jwt), userDTO);
       response.put("userSuccess", success);
       return response;

    }

    @PostMapping("verify_email")
    public Map<String, Boolean> verifyUserEmail(@RequestBody VerifyEmailRequestDTO emailDto){

        Map<String, Boolean> res = new HashMap<>();
        String expectedCode = redisService.get(CONFIRMING_EMAIL+":"+emailDto.getEmail());


        if (expectedCode == null) {
            res.put("expired", true);
        } else if (!expectedCode.trim().equals(emailDto.getUserCode().trim())) {
            res.put("notMatch", true);
        } else {
            res.put("success", true);
            redisService.saveTemp(CONFIRMED_EMAIL+":"+emailDto.getEmail(),"",3600);
        }

        return res;
    }


    @GetMapping("/info")
    public ResponseEntity<UserResponseDTO> getUserInfo(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        return ResponseEntity.ok(userService.collectCommonUserInfo(getUserKeycloakId(jwt)));
}

    @GetMapping("/email_changed")
    public ResponseEntity<Boolean> userEmailIsChanged(@RequestParam @Email String email, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(userKeycloakService.userEmailIsChanged(getUserKeycloakId(jwt),email));
    }


    @PostMapping("/login")
    @PreAuthorize("!isAuthenticated()")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequestDTO loginRequestDTO, BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            return ResponseEntity
                    .badRequest()
                    .body(bindingResult.getAllErrors()
                            .stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .toList());
        }

        Optional<String> optionalToken = userKeycloakService.generateJwtToken(loginRequestDTO.getNickName(), loginRequestDTO.getPassword());

        if(optionalToken.isEmpty()) {
            return ResponseEntity.badRequest()
                    .build();
        }

        ResponseCookie responseCookie = ResponseCookie.from("jwt", optionalToken.get())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofHours(118))
                .build();

        return ResponseEntity.ok()
                 .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                 .body("Login successful!");


    }

    @GetMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Jwt jwt){

        userKeycloakService.logout(getUserKeycloakId(jwt));


        ResponseCookie responseCookie = ResponseCookie.from("jwt","")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body("Logout successful!");


    }

    @PostMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> isAuthenticated(@AuthenticationPrincipal Jwt jwt){
            return ResponseEntity
                    .ok()
                    .build();


        }




    }
