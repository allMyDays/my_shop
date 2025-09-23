package com.example.user_service.controller.rest;

import com.example.common.dto.user.UpdateUserRequestDTO;
import com.example.common.dto.user.CreateUserRequestDTO;
import com.example.common.dto.user.UserResponseDTO;
import com.example.common.enumeration.grpc.UserExistenceStatus;
import com.example.common.exception.UserNotFoundException;
import com.example.common.service.CommonUserService;
import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.ResetPasswordDTO;
import com.example.user_service.dto.VerifyEmailRequestDTO;
import com.example.user_service.enumeration.EmailConfirmationStatus;
import com.example.user_service.enumeration.PasswordResettingStatus;
import com.example.user_service.enumeration.UserCreationStatus;
import com.example.user_service.enumeration.UserUpdateStatus;
import com.example.user_service.service.RedisService;
import com.example.user_service.service.UserKeycloakService;
import com.example.user_service.service.UserService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.common.enumeration.grpc.UserExistenceStatus.EMAIL_EXISTS;
import static com.example.common.enumeration.grpc.UserExistenceStatus.NOT_EXISTS;
import static com.example.common.service.CommonUserService.getMyUserEntityId;
import static com.example.common.service.CommonUserService.getUserKeycloakId;
import static com.example.user_service.enumeration.EmailConfirmationStatus.*;
import static com.example.user_service.enumeration.RedisSubKeys.*;

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
    public Map<UserCreationStatus, Object> createUser(@Validated @RequestBody CreateUserRequestDTO userDTO, BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            return Map.of(UserCreationStatus.ERRORS, bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList());

        } if(!userDTO.getPassword().equals(userDTO.getRepeatedPassword())){
            return Map.of(UserCreationStatus.ERRORS, List.of("Пароли не совпадают."));
        }
        UserExistenceStatus existenceStatus = userKeycloakService.userExists(userDTO.getEmail(),userDTO.getNickName(),null);

        if (!existenceStatus.equals(NOT_EXISTS)) {
            return Map.of(UserCreationStatus.ERRORS, List.of("Пользователь с таким %s уже существует".formatted(existenceStatus.equals(EMAIL_EXISTS)?"email":"никнеймом")));
        }

        if (!userService.userEmailIsVerifiedOrSendCodeOtherwise(userDTO.getEmail())) {
            return Map.of(UserCreationStatus.EMAIL_SENT,true);
        }
        boolean success = userService.createCommonUser(userDTO);
        return Map.of(UserCreationStatus.SUCCESS,success);

    }

    @PostMapping("/reset_password")
    @PreAuthorize("!isAuthenticated()")
    public Map<PasswordResettingStatus, Object> resetUserPassword(@Validated @RequestBody ResetPasswordDTO passwordDTO, BindingResult res) {

        if (res.hasErrors()) {
            return Map.of(PasswordResettingStatus.ERRORS, res.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
        } if(!passwordDTO.getNewPassword().equals(passwordDTO.getRepeatedPassword())){
            return Map.of(PasswordResettingStatus.ERRORS, List.of("Пароли не совпадают."));
        }
        Optional<UserRepresentation> optionalUser = userKeycloakService.getUser(passwordDTO.getNickName());
        if (optionalUser.isEmpty()){
           return Map.of(PasswordResettingStatus.ERRORS, List.of("Пользователь с таким никнеймом не найден."));
        }
        UserRepresentation userRep = optionalUser.get();
          if(!passwordDTO.getEmail().equalsIgnoreCase(userRep.getEmail())){
              return Map.of(PasswordResettingStatus.ERRORS, List.of("Введенный email не совпадает с тем, который привязан к аккаунту с таким никнеймом."));
          }
          if(userService.userEmailIsVerifiedOrSendCodeOtherwise(userRep.getEmail())){
             if(userKeycloakService.setUserPassword(userRep.getId(),passwordDTO.getNewPassword())){
                 return Map.of(PasswordResettingStatus.SUCCESS,true);
             } else{
                 return Map.of(PasswordResettingStatus.ERRORS,List.of("Не удалось изменить пароль. Пожалуйста, попробуйте позже."));
             }
        } else{
            int atIndex = userRep.getEmail().indexOf('@');
            String resultEmail = userRep.getEmail().charAt(0) + "*".repeat(atIndex - 1) + userRep.getEmail().substring(atIndex);  //заблюриваю емаил
            return Map.of(PasswordResettingStatus.EMAIL_SENT,resultEmail);
        }

    }


   @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public Map<UserUpdateStatus, Object> updateUser(@Validated @RequestBody UpdateUserRequestDTO userDTO, BindingResult res, @AuthenticationPrincipal Jwt jwt) {

       if (res.hasErrors()) {
            return Map.of(UserUpdateStatus.ERRORS, res.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
        } if(!userDTO.getPassword().equals(userDTO.getRepeatedPassword())){
           return Map.of(UserUpdateStatus.ERRORS, List.of("Пароли не совпадают."));
       }

        if(userDTO.getEmail()!=null){
            if(userKeycloakService.userEmailIsChanged(getUserKeycloakId(jwt), userDTO.getEmail())){
                if(userKeycloakService.userExists(userDTO.getEmail(), null, getUserKeycloakId(jwt)).equals(EMAIL_EXISTS)){
                    return Map.of(UserUpdateStatus.ERRORS, List.of("Пользователь с таким email уже существует."));
                }
                if(!userService.userEmailIsVerifiedOrSendCodeOtherwise(userDTO.getEmail())){
                    return Map.of(UserUpdateStatus.EMAIL_SENT,true);
                }
            } else{
                userDTO.setEmail(null);
            }
        }

       boolean success = userKeycloakService.updateUserData(getUserKeycloakId(jwt), userDTO);
       return Map.of(UserUpdateStatus.SUCCESS,success);

    }

    @PostMapping("verify_email")
    public EmailConfirmationStatus verifyUserEmail(@RequestBody VerifyEmailRequestDTO emailDto){

        String expectedCode = redisService.get(CONFIRMING_EMAIL+":"+emailDto.getEmail());


        if (expectedCode == null) {
            return EXPIRED;
        } else if (!expectedCode.trim().equals(emailDto.getUserCode().trim())) {
            String attemptNumber= redisService.get(CONFIRMING_EMAIL_ATTEMPT_NUMBER+":"+emailDto.getEmail());
            int attemptNumberInt = attemptNumber==null?0:Integer.parseInt(attemptNumber);
            if(attemptNumberInt>=4){
                return TOO_MANY_ATTEMPTS;
            }
            else{
                redisService.saveTemp(CONFIRMING_EMAIL_ATTEMPT_NUMBER+":"+emailDto.getEmail(), String.valueOf(++attemptNumberInt),3600);
                return NOT_MATCH;
            }

        } else {
            redisService.saveTemp(CONFIRMED_EMAIL+":"+emailDto.getEmail(),"",3600);
            redisService.delete(CONFIRMING_EMAIL+":"+emailDto.getEmail());
            return SUCCESS;
        }
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
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequestDTO loginRequestDTO, BindingResult bindingResult) throws UserNotFoundException {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(bindingResult.getAllErrors()
                            .stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .toList());
        }

        Optional<Jwt> optionalToken = userKeycloakService.generateJwtToken(loginRequestDTO.getNickName(), loginRequestDTO.getPassword());

        if(optionalToken.isEmpty()){
            return ResponseEntity.badRequest()
                    .body(List.of("Не удалось войти. Проверьте правильность введенных данных."));
        }
        Jwt token = optionalToken.get();

        if(userService.getUserOptionalEntity(getMyUserEntityId(token)).isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(List.of("Данные пользователя отсутствуют в основной базе данных. Пожалуйста, сообщите в поддержку."));

        }

        ResponseCookie responseCookie = ResponseCookie.from("jwt", token.getTokenValue())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofHours(4))
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
