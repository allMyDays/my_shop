package com.example.support_service.unit.controller;

import com.example.common.dto.support.SupportChatResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.support_service.controller.rest.user.SupportChatRestController;
import com.example.support_service.dto.CreateChatDto;
import com.example.support_service.entity.SupportChat;
import com.example.support_service.mapper.SupportChatMapper;
import com.example.support_service.service.SupportUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportChatRestControllerUnitTest {

    @Mock
    private SupportUserService supportUserService;

    @Mock
    private SupportChatMapper supportChatMapper;

    @Mock
    private Jwt jwt;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private SupportChatRestController supportChatRestController;

    private final Long TEST_USER_ID = 123L;
    private final Long TEST_CHAT_ID = 456L;

    @Test
    void createSupportChat_WhenValidRequest_ShouldReturnChatIdAndUserId() throws UserNotFoundException {
        // Arrange
        CreateChatDto createChatDto = new CreateChatDto();
        createChatDto.setTopic("Test Topic");

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(supportUserService.createSupportChat(eq(TEST_USER_ID), eq("Test Topic")))
                .thenReturn(TEST_CHAT_ID);

        // Act
        ResponseEntity<?> response = supportChatRestController.createSupportChat(jwt, createChatDto, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        //"unchecked"
        Map<String, Long> responseBody = (Map<String, Long>) response.getBody();
        assertEquals(TEST_CHAT_ID, responseBody.get("chatId"));
        assertEquals(TEST_USER_ID, responseBody.get("userId"));

        verify(supportUserService).createSupportChat(TEST_USER_ID, "Test Topic");
    }

    @Test
    void createSupportChat_WhenValidationErrors_ShouldReturnBadRequest() throws UserNotFoundException {
        // Arrange
        CreateChatDto createChatDto = new CreateChatDto();
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors())
                .thenReturn(List.of(new ObjectError("topic", "Topic is required")));

        // Act
        ResponseEntity<?> response = supportChatRestController.createSupportChat(jwt, createChatDto, bindingResult);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        verify(supportUserService, never()).createSupportChat(anyLong(), anyString());
    }

    @Test
    void createSupportChat_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        CreateChatDto createChatDto = new CreateChatDto();
        createChatDto.setTopic("Test Topic");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(supportUserService.createSupportChat(eq(TEST_USER_ID), anyString()))
                .thenThrow(new UserNotFoundException());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            supportChatRestController.createSupportChat(jwt, createChatDto, bindingResult);
        });
    }

    @Test
    void checkChatCreationAbility_WhenNotLimited_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        when(supportUserService.isChatCreationLimited(eq(TEST_USER_ID))).thenReturn(false);
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());

        // Act
        ResponseEntity<?> response = supportChatRestController.checkChatCreationAbility(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(supportUserService).isChatCreationLimited(TEST_USER_ID);
    }

    @Test
    void checkChatCreationAbility_WhenLimited_ShouldReturnTooManyRequests() throws UserNotFoundException {
        // Arrange
        when(supportUserService.isChatCreationLimited(eq(TEST_USER_ID))).thenReturn(true);
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());

        // Act
        ResponseEntity<?> response = supportChatRestController.checkChatCreationAbility(jwt);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("You temporarily exhausted the limit of creation support chats", response.getBody());
        verify(supportUserService).isChatCreationLimited(TEST_USER_ID);
    }

    @Test
    void deleteSupportChat_WhenValidRequest_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doNothing().when(supportUserService).deleteSupportChat(eq(TEST_USER_ID), eq(TEST_CHAT_ID));

        // Act
        ResponseEntity<?> response = supportChatRestController.deleteSupportChat(jwt, TEST_CHAT_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(supportUserService).deleteSupportChat(TEST_USER_ID, TEST_CHAT_ID);
    }

    @Test
    void deleteSupportChat_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doThrow(new UserNotFoundException())
                .when(supportUserService).deleteSupportChat(eq(TEST_USER_ID), eq(TEST_CHAT_ID));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            supportChatRestController.deleteSupportChat(jwt, TEST_CHAT_ID);
        });
    }

    @Test
    void getAllUserSupportChats_WhenValidRequest_ShouldReturnChatList() throws UserNotFoundException {
        // Arrange
        List<SupportChat> supportChats = List.of(new SupportChat(), new SupportChat());
        List<SupportChatResponseDTO> expectedDTOs = List.of(
                new SupportChatResponseDTO(), new SupportChatResponseDTO()
        );

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(supportUserService.getAllUserSupportChats(eq(TEST_USER_ID))).thenReturn(supportChats);
        when(supportChatMapper.toSupportChatDTOList(eq(supportChats))).thenReturn(expectedDTOs);

        // Act
        List<SupportChatResponseDTO> result = supportChatRestController.getAllUserSupportChats(jwt);

        // Assert
        assertEquals(expectedDTOs, result);
        verify(supportUserService).getAllUserSupportChats(TEST_USER_ID);
        verify(supportChatMapper).toSupportChatDTOList(supportChats);
    }

    @Test
    void getAllUserSupportChats_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(supportUserService.getAllUserSupportChats(eq(TEST_USER_ID)))
                .thenThrow(new UserNotFoundException());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            supportChatRestController.getAllUserSupportChats(jwt);
        });
    }

    @Test
    void getAllUserSupportChats_WhenNoChats_ShouldReturnEmptyList() throws UserNotFoundException {
        // Arrange
        List<SupportChat> emptyChats = List.of();
        List<SupportChatResponseDTO> emptyDTOs = List.of();

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(supportUserService.getAllUserSupportChats(eq(TEST_USER_ID))).thenReturn(emptyChats);
        when(supportChatMapper.toSupportChatDTOList(eq(emptyChats))).thenReturn(emptyDTOs);

        // Act
        List<SupportChatResponseDTO> result = supportChatRestController.getAllUserSupportChats(jwt);

        // Assert
        assertTrue(result.isEmpty());
        verify(supportUserService).getAllUserSupportChats(TEST_USER_ID);
        verify(supportChatMapper).toSupportChatDTOList(emptyChats);
    }

    // Тесты для проверки аннотаций безопасности и маппинга
    @Test
    void controllerClass_ShouldHaveCorrectAnnotations() {
        Class<SupportChatRestController> controllerClass = SupportChatRestController.class;

        assertTrue(controllerClass.isAnnotationPresent(RestController.class));
        assertTrue(controllerClass.isAnnotationPresent(PreAuthorize.class));
        assertTrue(controllerClass.isAnnotationPresent(RequestMapping.class));

        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/api/support/chat"}, requestMapping.value());

        PreAuthorize preAuthorize = controllerClass.getAnnotation(PreAuthorize.class);
        assertEquals("isAuthenticated()", preAuthorize.value());
    }

    @Test
    void methods_ShouldHaveCorrectAnnotations() throws NoSuchMethodException {
        Class<SupportChatRestController> controllerClass = SupportChatRestController.class;

        // Проверка createSupportChat
        var createMethod = controllerClass.getMethod("createSupportChat", Jwt.class,
                CreateChatDto.class, BindingResult.class);
        assertTrue(createMethod.isAnnotationPresent(PostMapping.class));
        PostMapping postMapping = createMethod.getAnnotation(PostMapping.class);
        assertArrayEquals(new String[]{"/create"}, postMapping.value());

        // Проверка checkChatCreationAbility
        var checkMethod = controllerClass.getMethod("checkChatCreationAbility", Jwt.class);
        assertTrue(checkMethod.isAnnotationPresent(GetMapping.class));
        GetMapping getMapping = checkMethod.getAnnotation(GetMapping.class);
        assertArrayEquals(new String[]{"/create-ability"}, getMapping.value());

        // Проверка deleteSupportChat
        var deleteMethod = controllerClass.getMethod("deleteSupportChat", Jwt.class, Long.class);
        assertTrue(deleteMethod.isAnnotationPresent(DeleteMapping.class));
        DeleteMapping deleteMapping = deleteMethod.getAnnotation(DeleteMapping.class);
        assertArrayEquals(new String[]{"/delete"}, deleteMapping.value());

        // Проверка getAllUserSupportChats
        var getAllMethod = controllerClass.getMethod("getAllUserSupportChats", Jwt.class);
        assertTrue(getAllMethod.isAnnotationPresent(GetMapping.class));
        GetMapping getAllMapping = getAllMethod.getAnnotation(GetMapping.class);
        assertArrayEquals(new String[]{"/get_all"}, getAllMapping.value());
    }
}