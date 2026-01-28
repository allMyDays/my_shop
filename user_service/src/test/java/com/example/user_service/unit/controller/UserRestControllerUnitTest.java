package com.example.user_service.unit.controller;

import com.example.user_service.dto.CreateUserRequestDTO;
import com.example.common.enumeration.user.UserExistenceStatus;
import com.example.common.service.CommonUserService;
import com.example.user_service.controller.rest.UserRestController;
import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.VerifyEmailRequestDTO;
import com.example.user_service.enumeration.EmailConfirmationStatus;
import com.example.user_service.enumeration.UserCreationStatus;
import com.example.user_service.service.RedisService;
import com.example.user_service.service.UserKeycloakService;
import com.example.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import static com.example.user_service.enumeration.RedisSubKeys.CONFIRMING_EMAIL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserKeycloakService userKeycloakService;

    @Mock
    private RedisService redisService;

    @Mock
    private Jwt jwt;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private UserRestController userRestController;

    private final String TEST_USER_ID = "test-user-id";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_NICKNAME = "testuser";

    @Test
    void getUserRoles_WhenRolesExist_ShouldReturnRoles() {
        // Arrange
        List<String> expectedRoles = List.of("ROLE_USER", "ROLE_ADMIN");

        try (var mockedCommonUserService = mockStatic(CommonUserService.class)) {
            mockedCommonUserService.when(() -> CommonUserService.getUserRoles(jwt))
                    .thenReturn(Optional.of(expectedRoles));

            // Act
            ResponseEntity<List<String>> response = userRestController.getUserRoles(jwt);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedRoles, response.getBody());
        }
    }

    @Test
    void getUserRoles_WhenNoRoles_ShouldReturnBadRequest() {
        // Arrange
        try (var mockedCommonUserService = mockStatic(CommonUserService.class)) {
            mockedCommonUserService.when(() -> CommonUserService.getUserRoles(jwt))
                    .thenReturn(Optional.empty());

            // Act
            ResponseEntity<List<String>> response = userRestController.getUserRoles(jwt);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    //  вспомогательный метод для мока статических методов
    private void mockGetUserKeycloakId() {
        try {
            var method = UserRestController.class.getDeclaredMethod("getUserKeycloakId", Jwt.class);
            method.setAccessible(true);
            when(method.invoke(userRestController, jwt)).thenReturn(TEST_USER_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createUser_WhenValidationErrors_ShouldReturnErrors() {
        // Arrange
        CreateUserRequestDTO userDTO = new CreateUserRequestDTO(".....",".....",".....",TEST_EMAIL,"", "");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors())
                .thenReturn(List.of(new ObjectError("email", "Email is required")));

        // Act
        Map<UserCreationStatus, Object> result = userRestController.createUser(userDTO, bindingResult);

        // Assert
        assertTrue(result.containsKey(UserCreationStatus.ERRORS));
        assertInstanceOf(List.class, result.get(UserCreationStatus.ERRORS));
    }

    @Test
    void createUser_WhenPasswordsDontMatch_ShouldReturnErrors() {
        // Arrange
        CreateUserRequestDTO userDTO = new CreateUserRequestDTO(".....",".....",".....",TEST_EMAIL,"password1", "password2");
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        Map<UserCreationStatus, Object> result = userRestController.createUser(userDTO, bindingResult);

        // Assert
        assertTrue(result.containsKey(UserCreationStatus.ERRORS));
        List<String> errors = (List<String>) result.get(UserCreationStatus.ERRORS);
        assertTrue(errors.stream().anyMatch(error -> error.contains("Пароли не совпадают")));
    }

    @Test
    void createUser_WhenUserExists_ShouldReturnErrors() {
        // Arrange
        CreateUserRequestDTO userDTO = createValidUserDTO();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userKeycloakService.userExists(anyString(), anyString(), isNull()))
                .thenReturn(UserExistenceStatus.EMAIL_EXISTS);

        // Act
        Map<UserCreationStatus, Object> result = userRestController.createUser(userDTO, bindingResult);

        // Assert
        assertTrue(result.containsKey(UserCreationStatus.ERRORS));
    }

    @Test
    void createUser_WhenEmailNotVerified_ShouldReturnEmailSent() {
        // Arrange
        CreateUserRequestDTO userDTO = createValidUserDTO();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userKeycloakService.userExists(anyString(), anyString(), isNull()))
                .thenReturn(UserExistenceStatus.NOT_EXISTS);
        when(userService.userEmailIsVerifiedOrSendCodeOtherwise(anyString())).thenReturn(false);

        // Act
        Map<UserCreationStatus, Object> result = userRestController.createUser(userDTO, bindingResult);

        // Assert
        assertTrue(result.containsKey(UserCreationStatus.EMAIL_SENT));
        assertEquals(true, result.get(UserCreationStatus.EMAIL_SENT));
    }

    @Test
    void verifyUserEmail_WhenCodeExpired_ShouldReturnExpired() {
        // Arrange
        VerifyEmailRequestDTO emailDto = new VerifyEmailRequestDTO();
        emailDto.setEmail(TEST_EMAIL);
        emailDto.setUserCode("123456");

        when(redisService.get(eq(CONFIRMING_EMAIL+":" + TEST_EMAIL), eq(false)))
                .thenReturn(Optional.empty());

        // Act
        EmailConfirmationStatus result = userRestController.verifyUserEmail(emailDto);

        // Assert
        assertEquals(EmailConfirmationStatus.EXPIRED, result);
    }


    @Test
    void login_WhenInvalidCredentials_ShouldReturnBadRequest() {
        // Arrange
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(TEST_NICKNAME,"wrongpassword" );

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userKeycloakService.generateJwtToken(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userRestController.login(loginRequestDTO, bindingResult);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST,


                response.getStatusCode());
    }

    @Test
    void verifyUserEmail_WhenCodeMatches_ShouldReturnSuccess() {
        // Arrange
        VerifyEmailRequestDTO emailDto = new VerifyEmailRequestDTO();
        emailDto.setEmail(TEST_EMAIL);
        emailDto.setUserCode("123456");

        // Используем правильные ключи - те же самые, что в реальном коде
        String confirmingEmailKey = "CONFIRMING_EMAIL:" + TEST_EMAIL;
        String confirmedEmailKey = "CONFIRMED_EMAIL:" + TEST_EMAIL;

        when(redisService.get(eq(confirmingEmailKey), eq(false)))
                .thenReturn(Optional.of("123456"));

        // Act
        EmailConfirmationStatus result = userRestController.verifyUserEmail(emailDto);

        // Assert
        assertEquals(EmailConfirmationStatus.SUCCESS, result);

        // Проверяем, что сохранили подтвержденный email
        verify(redisService).saveTemp(eq(confirmedEmailKey), eq(""), eq(3600L));
        // Проверяем, что удалили ожидающий подтверждения код (правильный ключ!)
        verify(redisService).delete(eq(confirmingEmailKey));
    }

    @Test
    void logout_ShouldReturnSuccess() {
        // Arrange
        // Настройка JWT для возврата правильного user ID
        when(jwt.getClaim("sub")).thenReturn(TEST_USER_ID);

        // Act
        ResponseEntity<?> response = userRestController.logout(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful!", response.getBody());

        verify(userKeycloakService).logout(TEST_USER_ID);

        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("Max-Age=0")); // Cookie удаляется
    }


    @Test
    void isAuthenticated_ShouldReturnOk() {
        // Act
        ResponseEntity<?> response = userRestController.isAuthenticated(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Вспомогательные методы
    private CreateUserRequestDTO createValidUserDTO() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(".....",".....",".....",TEST_EMAIL,"password1", "password1");
        return dto;
    }
}