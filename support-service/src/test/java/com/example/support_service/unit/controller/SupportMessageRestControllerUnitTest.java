package com.example.support_service.unit.controller;

import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.support_service.controller.rest.user.SupportMessageRestController;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.mapper.SupportMessageMapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportMessageRestControllerUnitTest {

    @Mock
    private SupportUserService supportUserService;

    @Mock
    private SupportMessageMapper supportMessageMapper;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private SupportMessageRestController supportMessageRestController;

    private final Long TEST_USER_ID = 123L;
    private final Long TEST_CHAT_ID = 456L;

    @Test
    void getAllChatMessages_WhenValidRequest_ShouldReturnMessageList() throws UserNotFoundException {
        // Arrange
        List<SupportMessage> supportMessages = List.of(new SupportMessage(), new SupportMessage());
        List<SupportMessageResponseDTO> expectedDTOs = List.of(
                new SupportMessageResponseDTO(), new SupportMessageResponseDTO()
        );
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(supportUserService.getAllSupportChatMessages(eq(TEST_USER_ID), eq(TEST_CHAT_ID)))
                .thenReturn(supportMessages);
        when(supportMessageMapper.toSupportMessageDTOList(eq(supportMessages)))
                .thenReturn(expectedDTOs);

        // Act
        List<SupportMessageResponseDTO> result = supportMessageRestController.getAllChatMessages(TEST_CHAT_ID,jwt);

        // Assert
        assertEquals(expectedDTOs, result);
        verify(supportUserService).getAllSupportChatMessages(TEST_USER_ID, TEST_CHAT_ID);
        verify(supportMessageMapper).toSupportMessageDTOList(supportMessages);
    }

    @Test
    void getAllChatMessages_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(supportUserService.getAllSupportChatMessages(eq(TEST_USER_ID), eq(TEST_CHAT_ID)))


                .thenThrow(new UserNotFoundException());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            supportMessageRestController.getAllChatMessages(TEST_CHAT_ID, jwt);
        });

        verify(supportUserService).getAllSupportChatMessages(TEST_USER_ID, TEST_CHAT_ID);
        verify(supportMessageMapper, never()).toSupportMessageDTOList(any());
    }

    @Test
    void getAllChatMessages_WhenNoMessages_ShouldReturnEmptyList() throws UserNotFoundException {
        // Arrange
        List<SupportMessage> emptyMessages = List.of();
        List<SupportMessageResponseDTO> emptyDTOs = List.of();

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());

        when(supportUserService.getAllSupportChatMessages(eq(TEST_USER_ID), eq(TEST_CHAT_ID)))
                .thenReturn(emptyMessages);
        when(supportMessageMapper.toSupportMessageDTOList(eq(emptyMessages)))
                .thenReturn(emptyDTOs);

        // Act
        List<SupportMessageResponseDTO> result = supportMessageRestController.getAllChatMessages(TEST_CHAT_ID, jwt);

        // Assert
        assertTrue(result.isEmpty());
        verify(supportUserService).getAllSupportChatMessages(TEST_USER_ID, TEST_CHAT_ID);
        verify(supportMessageMapper).toSupportMessageDTOList(emptyMessages);
    }

    @Test
    void getAllChatMessages_WithDifferentChatId_ShouldCallServiceWithCorrectParameters() throws UserNotFoundException {
        // Arrange
        Long differentChatId = 789L;
        List<SupportMessage> supportMessages = List.of(new SupportMessage());
        List<SupportMessageResponseDTO> expectedDTOs = List.of(new SupportMessageResponseDTO());

        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());

        when(supportUserService.getAllSupportChatMessages(eq(TEST_USER_ID), eq(differentChatId)))
                .thenReturn(supportMessages);
        when(supportMessageMapper.toSupportMessageDTOList(eq(supportMessages)))
                .thenReturn(expectedDTOs);

        // Act
        List<SupportMessageResponseDTO> result = supportMessageRestController.getAllChatMessages(differentChatId, jwt);

        // Assert
        assertEquals(expectedDTOs, result);
        verify(supportUserService).getAllSupportChatMessages(TEST_USER_ID, differentChatId);
    }

    @Test
    void getAllChatMessages_WhenServiceThrowsRuntimeException_ShouldPropagate() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        when(supportUserService.getAllSupportChatMessages(eq(TEST_USER_ID), eq(TEST_CHAT_ID)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            supportMessageRestController.getAllChatMessages(TEST_CHAT_ID, jwt);
        });
    }

    // Тесты для проверки аннотаций безопасности и маппинга
    @Test
    void controllerClass_ShouldHaveCorrectAnnotations() {
        Class<SupportMessageRestController> controllerClass = SupportMessageRestController.class;

        assertTrue(controllerClass.isAnnotationPresent(RestController.class));
        assertTrue(controllerClass.isAnnotationPresent(PreAuthorize.class));
        assertTrue(controllerClass.isAnnotationPresent(RequestMapping.class));

        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/api/support/message"}, requestMapping.value());

        PreAuthorize preAuthorize = controllerClass.getAnnotation(PreAuthorize.class);
        assertEquals("isAuthenticated()", preAuthorize.value());
    }

    @Test
    void methods_ShouldHaveCorrectAnnotations() throws NoSuchMethodException {
        Class<SupportMessageRestController> controllerClass = SupportMessageRestController.class;

        // Проверка checkMessageSendingAbility
        var checkMethod = controllerClass.getMethod("checkMessageSendingAbility", Long.class);
        assertTrue(checkMethod.isAnnotationPresent(GetMapping.class));
        GetMapping getMapping = checkMethod.getAnnotation(GetMapping.class);
        assertArrayEquals(new String[]{"/send-ability"}, getMapping.value());

        // Проверка getAllChatMessages
        var getAllMethod = controllerClass.getMethod("getAllChatMessages", Jwt.class, Long.class);
        assertTrue(getAllMethod.isAnnotationPresent(GetMapping.class));
        GetMapping getAllMapping = getAllMethod.getAnnotation(GetMapping.class);
        assertArrayEquals(new String[]{"/get_all"}, getAllMapping.value());
    }
}