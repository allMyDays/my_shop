package com.example.user_service.unit.service;

import com.example.common.client.grpc.MediaGrpcClient;
import com.example.common.client.kafka.EmailKafkaClient;
import com.example.common.client.kafka.MediaKafkaClient;
import com.example.common.enumeration.user.KeycloakRole;
import com.example.common.mapper.MediaMapper;
import com.example.user_service.dto.CreateUserRequestDTO;
import com.example.common.dto.user.rest.UserResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.user_service.entity.MyUser;
import com.example.user_service.enumeration.UserCreationInnerStatus;
import com.example.user_service.exception.KeycloakException;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.RedisService;
import com.example.user_service.service.UserKeycloakService;
import com.example.user_service.service.UserService;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.example.user_service.enumeration.RedisSubKeys.CONFIRMED_EMAIL;
import static com.example.user_service.enumeration.UserCreationInnerStatus.KEYCLOAK_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserKeycloakService userKeycloakService;

    @Mock
    private EmailKafkaClient emailKafkaClient;

    @Mock
    private RedisService redisService;

    @Mock
    private MediaKafkaClient mediaKafkaClient;

    @Mock
    private MediaGrpcClient mediaGrpcClient;

    @Mock
    private MediaMapper mediaMapper;

    private final Long USER_ID = 1L;
    private final String KEYCLOAK_ID = "keycloak-123";
    private final String EMAIL = "test@example.com";
    private final KeycloakRole ROLE = KeycloakRole.ROLE_CUSTOMER;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userKeycloakService, emailKafkaClient, redisService, mediaKafkaClient, mediaGrpcClient, mediaMapper);

        ReflectionTestUtils.setField(userService, "selfLink", userService);
    }

    @Test
    void collectCommonUserInfo_WithKeycloakId_ReturnsUserInfo() throws UserNotFoundException {
        // Given
        UserResponseDTO keycloakResponse = new UserResponseDTO();
        keycloakResponse.setEmail(EMAIL);

        MyUser userEntity = new MyUser();
        userEntity.setId(USER_ID);
        userEntity.setAvatarFileName("avatar.jpg");

        when(userKeycloakService.collectKeycloakUserInfo(KEYCLOAK_ID))
                .thenReturn(Optional.of(keycloakResponse));
        when(userRepository.findByKeycloakId(KEYCLOAK_ID))
                .thenReturn(Optional.of(userEntity));

        // When
        UserResponseDTO result = userService.collectCommonUserInfo(KEYCLOAK_ID);

        // Then
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        assertEquals("avatar.jpg", result.getAvatarFileName());
        assertEquals(EMAIL, result.getEmail());

        verify(userKeycloakService).collectKeycloakUserInfo(KEYCLOAK_ID);
        verify(userRepository).findByKeycloakId(KEYCLOAK_ID);
    }

    @Test
    void collectCommonUserInfo_WithKeycloakId_WhenUserNotFound_ThrowsUserNotFoundException() {
        // Given
        when(userKeycloakService.collectKeycloakUserInfo(KEYCLOAK_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> userService.collectCommonUserInfo(KEYCLOAK_ID));

        verify(userRepository, never()).findByKeycloakId(anyString());
    }

    @Test
    void collectCommonUserInfo_WithUserId_ReturnsUserInfo() throws UserNotFoundException {
        // Given
        MyUser userEntity = new MyUser();
        userEntity.setId(USER_ID);
        userEntity.setKeycloakId(KEYCLOAK_ID);

        UserResponseDTO keycloakResponse = new UserResponseDTO();
        keycloakResponse.setEmail(EMAIL);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
        when(userKeycloakService.collectKeycloakUserInfo(KEYCLOAK_ID))
                .thenReturn(Optional.of(keycloakResponse));
        when(userRepository.findByKeycloakId(KEYCLOAK_ID))
                .thenReturn(Optional.of(userEntity));

        // When
        UserResponseDTO result = userService.collectCommonUserInfo(USER_ID);

        // Then
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        assertEquals(EMAIL, result.getEmail());

        verify(userRepository).findById(USER_ID);
    }

    @Test
    void collectCommonUserInfo_WithUserId_WhenUserNotFound_ThrowsUserNotFoundException() {


// Given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> userService.collectCommonUserInfo(USER_ID));
    }


    @Test
    void deleteUserAvatar_WithExistingAvatar_DeletesAvatar() {
        // Given
        MyUser userEntity = new MyUser();
        userEntity.setId(USER_ID);
        userEntity.setAvatarFileName("avatar.jpg");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        // When
        userService.deleteUserAvatar(USER_ID);

        // Then
        assertNull(userEntity.getAvatarFileName());
        verify(userRepository).save(userEntity);
        verify(mediaKafkaClient).deleteMedia(List.of("avatar.jpg"));
    }

    @Test
    void deleteUserAvatar_WithNoAvatar_DoesNothing() {
        // Given
        MyUser userEntity = new MyUser();
        userEntity.setId(USER_ID);
        userEntity.setAvatarFileName(null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));

        // When
        userService.deleteUserAvatar(USER_ID);

        // Then
        verify(userRepository, never()).save(any());
        verify(mediaKafkaClient, never()).deleteMedia(anyList());
    }

    @Test
    void userEmailIsVerifiedOrSendCodeOtherwise_WhenEmailConfirmed_ReturnsTrue()  {
        // Given
        when(redisService.get(eq("CONFIRMED_EMAIL:" + EMAIL), eq(true)))
                .thenReturn(Optional.of("true"));

        // When
        boolean result = userService.userEmailIsVerifiedOrSendCodeOtherwise(EMAIL);

        // Then
        assertTrue(result);
        verify(redisService).delete(anyList());
        verify(emailKafkaClient, never()).sendSimpleMail(anyString(), anyString(), anyString());
    }


    @Test
    void getUserEntity_WithKeycloakId_ReturnsUser() {
        // Given
        MyUser expectedUser = new MyUser();
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(expectedUser));

        // When
        MyUser result = userService.getUserEntity(KEYCLOAK_ID);

        // Then
        assertEquals(expectedUser, result);
        verify(userRepository).findByKeycloakId(KEYCLOAK_ID);
    }

    @Test
    void getUserEntity_WithKeycloakId_WhenNotFound_ThrowsNotFoundException() {
        // Given
        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> userService.getUserEntity(KEYCLOAK_ID));
    }

    @Test
    void getUserEntity_WithUserId_ReturnsUser() {
        // Given
        MyUser expectedUser = new MyUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(expectedUser));

        // When
        MyUser result = userService.getUserEntity(USER_ID);

        // Then
        assertEquals(expectedUser, result);
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void getUserEntity_WithUserId_WhenNotFound_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> userService.getUserEntity(USER_ID));
    }

    @Test
    void getUserOptionalEntity_ReturnsOptional() {
        // Given
        MyUser expectedUser = new MyUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(expectedUser));

        // When
        Optional<MyUser> result = userService.getUserOptionalEntity(USER_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
    }

    @Test
    void saveUserEntity_SavesAndFlushes() {
        // Given
        MyUser user = new MyUser();
        MyUser savedUser = new MyUser();
        when(userRepository.save(user)).thenReturn(savedUser);

        // When
        MyUser result = userService.saveUserEntity(user);

        // Then
        assertEquals(savedUser, result);
        verify(userRepository).save(user);
        verify(userRepository).flush();
    }

    @Test
    void getOrCreateMyUser_WhenUserExists_ReturnsUser() {
        // Given
        MyUser existingUser = new MyUser();
        existingUser.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        // When
        MyUser result = userService.getOrCreateMyUser(USER_ID, KEYCLOAK_ID);

        // Then
        assertEquals(existingUser, result);
        verify(userRepository, never()).insertUserNativeQuery(anyLong(), anyString());
    }

    @Test
    void getOrCreateMyUser_WhenUserNotExists_CreatesUser() {
        // Given
        MyUser newUser = new MyUser();
        newUser.setId(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(newUser));
        doNothing().when(userRepository).insertUserNativeQuery(USER_ID, KEYCLOAK_ID);

        // When
        MyUser result = userService.getOrCreateMyUser(USER_ID, KEYCLOAK_ID);

        // Then
        assertEquals(newUser, result);
        verify(userRepository).insertUserNativeQuery(USER_ID, KEYCLOAK_ID);
    }



    @Test
    void userEmailIsVerifiedOrSendCodeOtherwise_WhenEmailNotConfirmed_SendsCodeAndReturnsFalse() throws MailSendException {
        // Given
        when(redisService.get(eq(CONFIRMED_EMAIL+":" + EMAIL), eq(true)))
                .thenReturn(Optional.empty());

        // When

        boolean result = userService.userEmailIsVerifiedOrSendCodeOtherwise(EMAIL);

        // Then
        assertFalse(result);
        verify(emailKafkaClient).sendSimpleMail(eq(EMAIL), anyString(), anyString());
    }




    @Test
    void createCommonUser_WhenKeycloakFails_ReturnsKEYCLOAK_ERROR() {
        // Given
        CreateUserRequestDTO userDTO = new CreateUserRequestDTO("testuser", "John", "Doe", EMAIL, "password123", "password123");

        MyUser savedUser = new MyUser();
        savedUser.setId(USER_ID);

        when(userRepository.save(any(MyUser.class))).thenReturn(savedUser);
        when(userKeycloakService.createKeycloakUser(anyString(), anyString(), anyString(), anyString(), anyLong(), eq(ROLE)))
                .thenThrow(new KeycloakException(new RuntimeException("error")));

        // When
        UserCreationInnerStatus status = userService.createCommonUser(userDTO, ROLE);

        // Then
        assertEquals(UserCreationInnerStatus.KEYCLOAK_ERROR, status);
        verify(userRepository, times(1)).save(any(MyUser.class));  // только первое сохранение
        verify(userKeycloakService, never()).setPasswordForUser(anyString(), anyString());
        verify(userKeycloakService, never()).deleteUser(anyString());
    }

    @Test
    void createCommonUser_WhenPasswordSettingFails_ReturnsINTERNAL_ERROR() {
        // Given
        CreateUserRequestDTO userDTO = new CreateUserRequestDTO("testuser", "John", "Doe", EMAIL, "password123", "password123");

        MyUser savedUser = new MyUser();
        savedUser.setId(USER_ID);

        when(userRepository.save(any(MyUser.class))).thenReturn(savedUser);
        when(userKeycloakService.createKeycloakUser(anyString(), anyString(), anyString(), anyString(), anyLong(), eq(ROLE)))
                .thenReturn(KEYCLOAK_ID);
        doThrow(new RuntimeException("Password error"))
                .when(userKeycloakService).setPasswordForUser(eq(KEYCLOAK_ID), eq("password123"));

        // When
        UserCreationInnerStatus status = userService.createCommonUser(userDTO, ROLE);

        // Then
        assertEquals(UserCreationInnerStatus.INTERNAL_ERROR, status);
        verify(userRepository, times(1)).save(any(MyUser.class));  // только первое сохранение
        verify(userKeycloakService).createKeycloakUser(anyString(), anyString(), anyString(), anyString(), anyLong(), eq(ROLE));
        verify(userKeycloakService).setPasswordForUser(KEYCLOAK_ID, "password123");
        verify(userKeycloakService).deleteUser(KEYCLOAK_ID);       // очистка созданного пользователя Keycloak
        verify(userRepository, never()).save(savedUser);           // повторное сохранение не вызывалось
    }

    @Test
    void createCommonUser_WithValidData_ReturnsSUCCESS() {
        // Given
        CreateUserRequestDTO userDTO = new CreateUserRequestDTO("testuser", "John", "Doe", EMAIL, "password123", "password123");

        MyUser savedUser = new MyUser();
        savedUser.setId(USER_ID);

        when(userRepository.save(any(MyUser.class))).thenReturn(savedUser);
        when(userKeycloakService.createKeycloakUser(anyString(), anyString(), anyString(), anyString(), anyLong(), eq(ROLE)))
                .thenReturn(KEYCLOAK_ID);
        when(userKeycloakService.setPasswordForUser(KEYCLOAK_ID, "password123")).thenReturn(true);

        // When
        UserCreationInnerStatus status = userService.createCommonUser(userDTO, ROLE);

        // Then
        assertEquals(UserCreationInnerStatus.SUCCESS, status);
        verify(userRepository, times(2)).save(any(MyUser.class));  // два сохранения: начальное и с Keycloak ID
        verify(userKeycloakService).createKeycloakUser("testuser", "John", "Doe", EMAIL, USER_ID, ROLE);
        verify(userKeycloakService).setPasswordForUser(KEYCLOAK_ID, "password123");
        verify(userKeycloakService, never()).deleteUser(anyString());
    }












}