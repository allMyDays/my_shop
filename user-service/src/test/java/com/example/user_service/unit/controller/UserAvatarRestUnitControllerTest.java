package com.example.user_service.unit.controller;

import com.example.common.exception.UserNotFoundException;
import com.example.user_service.controller.rest.UserAvatarRestController;
import com.example.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAvatarRestUnitControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Jwt jwt;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private UserAvatarRestController userAvatarRestController;

    private final Long TEST_USER_ID = 123L;

    @BeforeEach
    void setUp() {
      //  when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
    }


    @Test
    void deleteUserAvatar_WhenValidUser_ShouldReturnOk() throws UserNotFoundException {

        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());

        doNothing().when(userService).deleteUserAvatar(anyLong());

        // Act
        ResponseEntity<Void> response = userAvatarRestController.deleteUserAvatar(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deleteUserAvatar(eq(TEST_USER_ID));
    }

    @Test
    void deleteUserAvatar_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doThrow(new UserNotFoundException())
                .when(userService).deleteUserAvatar(anyLong());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userAvatarRestController.deleteUserAvatar(jwt);
        });

        verify(userService).deleteUserAvatar(eq(TEST_USER_ID));
    }

    @Test
    void deleteUserAvatar_WhenServiceCompletes_ShouldReturnOk() throws UserNotFoundException {
        // Arrange
        when(jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK)).thenReturn(TEST_USER_ID.toString());
        doNothing().when(userService).deleteUserAvatar(anyLong());

        // Act
        ResponseEntity<Void> response = userAvatarRestController.deleteUserAvatar(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).deleteUserAvatar(TEST_USER_ID);
    }

    // Тест для проверки безопасности - что методы действительно требуют аутентификации
    @Test
    void controllerMethods_ShouldHavePreAuthorizeAnnotations() {
        // Этот тест проверяет, что методы имеют нужные аннотации безопасности
        // Можно использовать рефлексию для проверки аннотаций

        Class<?> controllerClass = UserAvatarRestController.class;

        // Проверяем аннотацию класса
        assertTrue(controllerClass.isAnnotationPresent(PreAuthorize.class));
        PreAuthorize classAnnotation = controllerClass.getAnnotation(PreAuthorize.class);
        assertEquals("isAuthenticated()", classAnnotation.value());

        // Проверяем аннотации методов
        try {
            var uploadMethod = controllerClass.getMethod("uploadUserAvatar", MultipartFile.class, Jwt.class);
            var deleteMethod = controllerClass.getMethod("deleteUserAvatar", Jwt.class);

            assertTrue(uploadMethod.isAnnotationPresent(PreAuthorize.class));
            assertTrue(deleteMethod.isAnnotationPresent(PreAuthorize.class));

            PreAuthorize uploadAnnotation = uploadMethod.getAnnotation(PreAuthorize.class);
            PreAuthorize deleteAnnotation = deleteMethod.getAnnotation(PreAuthorize.class);

            assertEquals("isAuthenticated()", uploadAnnotation.value());
            assertEquals("isAuthenticated()", deleteAnnotation.value());

        } catch (NoSuchMethodException e) {
            fail("Methods not found: " + e.getMessage());
        }
    }

    // Дополнительные тесты для проверки mapping аннотаций
    @Test
    void controller_ShouldHaveCorrectRequestMapping() {
        Class<?> controllerClass = UserAvatarRestController.class;

        assertTrue(controllerClass.isAnnotationPresent(RequestMapping.class));
        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/api/users/avatar"}, requestMapping.value());
    }

    @Test
    void methods_ShouldHaveCorrectPostAndDeleteMappings() {
        Class<?> controllerClass = UserAvatarRestController.class;

        try {
            var uploadMethod = controllerClass.getMethod("uploadUserAvatar", MultipartFile.class, Jwt.class);
            var deleteMethod = controllerClass.getMethod("deleteUserAvatar", Jwt.class);

            assertTrue(uploadMethod.isAnnotationPresent(PostMapping.class));
            assertTrue(deleteMethod.isAnnotationPresent(DeleteMapping.class));

            PostMapping postMapping = uploadMethod.getAnnotation(PostMapping.class);
            DeleteMapping deleteMapping = deleteMethod.getAnnotation(DeleteMapping.class);

            assertArrayEquals(new String[]{"/upload"}, postMapping.value());
            assertArrayEquals(new String[]{"/delete"}, deleteMapping.value());

        } catch (NoSuchMethodException e) {
            fail("Methods not found: " + e.getMessage());
        }
    }
}