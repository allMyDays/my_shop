package com.example.support_service.unit.service;

import com.example.common.client.kafka.EmailKafkaClient;
import com.example.common.client.kafka.MediaKafkaClient;
import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.TooManyFunctionCallsException;
import com.example.common.exception.UserNotOwnerException;
import com.example.support_service.entity.SupportChat;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.repository.SupportChatRepository;
import com.example.support_service.repository.SupportMessageRepository;
import com.example.support_service.service.RedisService;
import com.example.support_service.service.SupportUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.example.support_service.enumeration.RedisSubKeys.SUPPORT_CHAT_CREATION_LIMIT;
import static com.example.support_service.enumeration.RedisSubKeys.SUPPORT_MESSAGE_SENDING_LIMIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceUnitTest {
    @Mock
    private RedisService redisService;

    @Mock
    private SupportChatRepository supportChatRepository;

    @Mock
    private SupportMessageRepository supportMessageRepository;

    @Mock
    private EmailKafkaClient emailKafkaClient;

    @Mock
    private MediaKafkaClient mediaKafkaClient;

    @InjectMocks
    private SupportUserService supportUserService;

    private final Long USER_ID = 1L;
    private final Long CHAT_ID = 100L;
    private final String TOPIC = "Test Topic";
    private final String MESSAGE = "Test message";
    private final String AGENT_EMAIL = "support@example.com";

    @Test
    void createSupportChat_WhenNotLimited_CreatesChatSuccessfully() {
        // Given
        String redisKey = SUPPORT_CHAT_CREATION_LIMIT + ":" + USER_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.empty());

        SupportChat newChat = new SupportChat();
        newChat.setId(CHAT_ID);
        newChat.setUserId(USER_ID);
        newChat.setTopic(TOPIC);
        when(supportChatRepository.save(any(SupportChat.class))).thenReturn(newChat);

        // Set agent email through reflection
        ReflectionTestUtils.setField(supportUserService, "agent_email", AGENT_EMAIL);

        // When
        Long result = supportUserService.createSupportChat(USER_ID, TOPIC);

        // Then
        assertEquals(CHAT_ID, result);
        verify(redisService, times(2)).get(redisKey);
        verify(supportChatRepository).save(argThat(chat ->
                chat.getUserId().equals(USER_ID) &&
                        chat.getTopic().equals(TOPIC) &&
                        !chat.isContainsMessages() &&
                        !chat.isNeedsAnswer()
        ));

        verify(redisService).saveTemp(eq(redisKey), eq("1"), eq(7200L));
    }

    @Test
    void createSupportChat_WhenAlreadyHasOneChat_IncrementsCounter() {
        // Given
        String redisKey = SUPPORT_CHAT_CREATION_LIMIT + ":" + USER_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("1"));

        SupportChat newChat = new SupportChat();
        newChat.setId(CHAT_ID);
        when(supportChatRepository.save(any(SupportChat.class))).thenReturn(newChat);
        ReflectionTestUtils.setField(supportUserService, "agent_email", AGENT_EMAIL);

        // When
        Long result = supportUserService.createSupportChat(USER_ID, TOPIC);

        // Then
        assertEquals(CHAT_ID, result);
        verify(redisService, times(2)).get(redisKey);
        // Исправлено: используем 7200L вместо 7200
        verify(redisService).saveTemp(eq(redisKey), eq("2"), eq(7200L));
    }

    @Test
    void saveSupportMessage_WithValidData_SavesMessageAndUpdatesChat() {
        // Given
        SupportChat supportChat = new SupportChat();
        supportChat.setId(CHAT_ID);
        supportChat.setUserId(USER_ID);

        String redisKey = SUPPORT_MESSAGE_SENDING_LIMIT + ":" + CHAT_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.empty());
        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.of(supportChat));
        when(supportChatRepository.save(supportChat)).thenReturn(supportChat);

        SupportMessage savedMessage = new SupportMessage();
        when(supportMessageRepository.save(any(SupportMessage.class))).thenReturn(savedMessage);

        ReflectionTestUtils.setField(supportUserService, "agent_email", AGENT_EMAIL);

        // When
        SupportMessage result = supportUserService.saveSupportMessage(USER_ID, CHAT_ID, MESSAGE,null);

        // Then
        assertNotNull(result);
        assertEquals(savedMessage, result);
        verify(supportChatRepository).findById(CHAT_ID);
        verify(redisService).get(redisKey);
        // Исправлено: используем 120L вместо 120
        verify(redisService).saveTemp(eq(redisKey), eq("1"), eq(120L));
        verify(supportChatRepository).save(argThat(chat ->
                chat.isNeedsAnswer() && chat.isContainsMessages()
        ));
        verify(emailKafkaClient).sendSimpleMail(
                eq(AGENT_EMAIL),
                eq("Новое сообщение в чат поддержки № %d".formatted(CHAT_ID)),
                eq("Сообщение:\n\n %s".formatted(MESSAGE))
        );
        verify(supportMessageRepository).save(argThat(message ->
                message.getMessage().equals(MESSAGE) &&
                        message.isUserMessage() &&
                        message.getChat().equals(supportChat)
        ));
    }

    @Test
    void saveSupportMessage_WithExistingMessageCounter_IncrementsCounter() {
        // Given
        SupportChat supportChat = new SupportChat();
        supportChat.setId(CHAT_ID);
        supportChat.setUserId(USER_ID);

        String redisKey = SUPPORT_MESSAGE_SENDING_LIMIT + ":" + CHAT_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("2"));
        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.of(supportChat));
        when(supportChatRepository.save(supportChat)).thenReturn(supportChat);
        when(supportMessageRepository.save(any(SupportMessage.class))).thenReturn(new SupportMessage());
        ReflectionTestUtils.setField(supportUserService, "agent_email", AGENT_EMAIL);

        // When
        supportUserService.saveSupportMessage(USER_ID, CHAT_ID, MESSAGE, null);

        // Then
        verify(redisService).get(redisKey);
        // Исправлено: используем 120L вместо 120
        verify(redisService).saveTemp(eq(redisKey), eq("3"), eq(120L));
    }

    @Test
    void createSupportChat_WhenLimited_ThrowsTooManyFunctionCallsException() {
        // Given
        String redisKey = SUPPORT_CHAT_CREATION_LIMIT + ":" + USER_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("7"));

        // When & Then
        assertThrows(TooManyFunctionCallsException.class,
                () -> supportUserService.createSupportChat(USER_ID, TOPIC));

        verify(supportChatRepository, never()).save(any(SupportChat.class));
        verify(redisService, never()).saveTemp(anyString(), anyString(), anyInt());
    }

    @Test
    void chatCreationIsLimited_WhenNoKey_ReturnsFalse() {
        // Given
        String redisKey = SUPPORT_CHAT_CREATION_LIMIT + ":" + USER_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.empty());

        // When
        boolean result = supportUserService.isChatCreationLimited(USER_ID);

        // Then
        assertFalse(result);
        verify(redisService).get(redisKey);
    }

    @Test
    void chatCreationIsLimited_WhenBelowLimit_ReturnsFalse() {
        // Given
        String redisKey = SUPPORT_CHAT_CREATION_LIMIT + ":" + USER_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("3"));

        // When
        boolean result = supportUserService.isChatCreationLimited(USER_ID);

        // Then
        assertFalse(result);
    }

    @Test
    void chatCreationIsLimited_WhenAtLimit_ReturnsTrue() {
        // Given
        String redisKey = SUPPORT_CHAT_CREATION_LIMIT + ":" + USER_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("7"));

        // When
        boolean result = supportUserService.isChatCreationLimited(USER_ID);

        // Then
        assertTrue(result);
    }

    @Test
    void chatCreationIsLimited_WhenAboveLimit_ReturnsTrue() {
        // Given
        String redisKey = SUPPORT_CHAT_CREATION_LIMIT + ":" + USER_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("9"));

        // When
        boolean result = supportUserService.isChatCreationLimited(USER_ID);

        // Then
        assertTrue(result);
    }

    @Test
    void messageSendingIsLimited_WhenNoKey_ReturnsFalse() {
        // Given
        String redisKey = SUPPORT_MESSAGE_SENDING_LIMIT + ":" + CHAT_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.empty());

        // When
        boolean result = supportUserService.isMessageSendingLimited(CHAT_ID);

        // Then
        assertFalse(result);
        verify(redisService).get(redisKey);
    }

    @Test
    void messageSendingIsLimited_WhenBelowLimit_ReturnsFalse() {
        // Given
        String redisKey = SUPPORT_MESSAGE_SENDING_LIMIT + ":" + CHAT_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("2"));

        // When
        boolean result = supportUserService.isMessageSendingLimited(CHAT_ID);

        // Then
        assertFalse(result);
    }

    @Test
    void messageSendingIsLimited_WhenAtLimit_ReturnsTrue() {
        // Given
        String redisKey = SUPPORT_MESSAGE_SENDING_LIMIT + ":" + CHAT_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("5"));

        // When
        boolean result = supportUserService.isMessageSendingLimited(CHAT_ID);

        // Then
        assertTrue(result);
    }

    @Test
    void messageSendingIsLimited_WhenAboveLimit_ReturnsTrue() {
        // Given
        String redisKey = SUPPORT_MESSAGE_SENDING_LIMIT + ":" + CHAT_ID;
        when(redisService.get(redisKey)).thenReturn(Optional.of("6"));

        // When
        boolean result = supportUserService.isMessageSendingLimited(CHAT_ID);

        // Then
        assertTrue(result);
    }


    @Test
    void saveSupportMessage_WhenChatNotFound_ThrowsEntityNotFoundException() {
        // Given
        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> supportUserService.saveSupportMessage(USER_ID, CHAT_ID, MESSAGE, null));

        verify(supportMessageRepository, never()).save(any(SupportMessage.class));
        verify(emailKafkaClient, never()).sendSimpleMail(anyString(), anyString(), anyString());
    }

    @Test
    void saveSupportMessage_WhenUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        SupportChat supportChat = new SupportChat();
        supportChat.setId(CHAT_ID);
        supportChat.setUserId(999L); // Different user

        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.of(supportChat));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> supportUserService.saveSupportMessage(USER_ID, CHAT_ID, MESSAGE, null));

        verify(supportMessageRepository, never()).save(any(SupportMessage.class));
    }

    @Test
    void getAllUserSupportChats_ReturnsUserChats() {
        // Given
        List<SupportChat> expectedChats = List.of(new SupportChat(), new SupportChat());
        when(supportChatRepository.findAllByUserIdOrderByIdDesc(USER_ID)).thenReturn(expectedChats);

        // When
        List<SupportChat> result = supportUserService.getAllUserSupportChats(USER_ID);

        // Then
        assertEquals(expectedChats, result);
        verify(supportChatRepository).findAllByUserIdOrderByIdDesc(USER_ID);
    }

    @Test
    void getAllSupportChatMessages_WithValidChat_ReturnsMessages() {
        // Given
        SupportChat supportChat = new SupportChat();
        supportChat.setId(CHAT_ID);
        supportChat.setUserId(USER_ID);

        List<SupportMessage> expectedMessages = List.of(new SupportMessage(), new SupportMessage());

        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.of(supportChat));
        when(supportMessageRepository.findAllByChatOrderById(supportChat)).thenReturn(expectedMessages);

        // When
        List<SupportMessage> result = supportUserService.getAllSupportChatMessages(USER_ID, CHAT_ID);

        // Then
        assertEquals(expectedMessages, result);
        verify(supportChatRepository).findById(CHAT_ID);
        verify(supportMessageRepository).findAllByChatOrderById(supportChat);
    }

    @Test
    void getAllSupportChatMessages_WhenUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        SupportChat supportChat = new SupportChat();
        supportChat.setId(CHAT_ID);
        supportChat.setUserId(999L); // Different user

        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.of(supportChat));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> supportUserService.getAllSupportChatMessages(USER_ID, CHAT_ID));

        verify(supportMessageRepository, never()).findAllByChatOrderById(any(SupportChat.class));
    }


    @Test
    void deleteSupportChat_WhenUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        SupportChat supportChat = new SupportChat();
        supportChat.setId(CHAT_ID);
        supportChat.setUserId(999L); // Different user

        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.of(supportChat));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> supportUserService.deleteSupportChat(USER_ID, CHAT_ID));

        verify(supportChatRepository, never()).delete(any(SupportChat.class));
    }

    @Test
    void validateEntityAndOwnership_WithValidOwnership_ReturnsChat() {
        // Given
        SupportChat expectedChat = new SupportChat();
        expectedChat.setId(CHAT_ID);
        expectedChat.setUserId(USER_ID);

        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.of(expectedChat));

        // When
        SupportChat result = supportUserService.validateChatEntityAndOwnership(USER_ID, CHAT_ID);

        // Then
        assertEquals(expectedChat, result);
        verify(supportChatRepository).findById(CHAT_ID);
    }

    @Test
    void validateEntityAndOwnership_WhenChatNotFound_ThrowsEntityNotFoundException() {
        // Given
        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> supportUserService.validateChatEntityAndOwnership(USER_ID, CHAT_ID));
    }

    @Test
    void validateEntityAndOwnership_WhenUserNotOwner_ThrowsUserNotOwnerException() {
        // Given
        SupportChat supportChat = new SupportChat();
        supportChat.setId(CHAT_ID);
        supportChat.setUserId(999L); // Different user

        when(supportChatRepository.findById(CHAT_ID)).thenReturn(Optional.of(supportChat));

        // When & Then
        assertThrows(UserNotOwnerException.class,
                () -> supportUserService.validateChatEntityAndOwnership(USER_ID, CHAT_ID));
    }

}