package com.example.user_service.unit.service;

import com.example.common.enumeration.user.KeycloakRole;
import com.example.user_service.dto.UpdateUserRequestDTO;
import com.example.common.dto.user.rest.UserResponseDTO;
import com.example.common.enumeration.user.UserExistenceStatus;
import com.example.user_service.dto.UserFullNameDto;
import com.example.user_service.service.UserKeycloakService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserKeycloakUnitServiceTest {

    @Mock
    private ObjectProvider<KeycloakBuilder> keycloakBuilderProvider;

    @Mock
    private Keycloak keycloakClientInstance;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private KeycloakBuilder keycloakBuilder;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private Response response;

    @Mock
    private Jwt jwt;

    @Mock
    private RealmResource realmResource;

    @InjectMocks
    private UserKeycloakService userKeycloakService;

    private final String realm = "test-realm";
    private final String userId = "test-user-id";
    private final String username = "testuser";
    private final String email = "test@example.com";
    private final String issuerUri = "http://localhost:8080/auth/realms/" + realm;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userKeycloakService, "issuerUri", issuerUri);
        userKeycloakService.setRealm();
    }

    // Тесты, которые не требуют моков Keycloak
    @Test
    void getUserRealm_WithValidJwt_ReturnsRealm() {
        // Arrange
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("iss")).thenReturn("http://localhost:8080/auth/realms/my-realm");

        // Act
        String result = userKeycloakService.getUserRealm(jwt);

        // Assert
        assertEquals("my-realm", result);
    }

    @Test
    void extractRealm_WithValidIssuerUri_ReturnsRealm() {
        // Act
        String result = UserKeycloakService.extractRealm("http://localhost:8080/auth/realms/my-realm");

        // Assert
        assertEquals("my-realm", result);
    }

    @Test
    void extractRealm_WithTrailingSlash_ReturnsRealm() {
        // Act
        String result = UserKeycloakService.extractRealm("http://localhost:8080/auth/realms/my-realm/");

        // Assert
        assertEquals("my-realm", result);
    }

    @Test
    void extractRealm_WithEmptyIssuerUri_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                UserKeycloakService.extractRealm(""));
    }

    @Test
    void extractServerUri_WithValidIssuerUri_ReturnsServerUri() {
        // Act
        String result = UserKeycloakService.extractServerUri("http://localhost:8080/auth/realms/my-realm");

        // Assert
        assertEquals("http://localhost:8080", result);
    }

    @Test
    void extractServerUri_WithHttpsIssuerUri_ReturnsServerUri() {
        // Act
        String result = UserKeycloakService.extractServerUri("https://keycloak.example.com/auth/realms/my-realm");

        // Assert
        assertEquals("https://keycloak.example.com", result);
    }

    @Test
    void extractServerUri_WithInvalidIssuerUri_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->


                UserKeycloakService.extractServerUri("invalid-uri"));
    }

    @Test
    void upperCaseFirstLetter_WithValidString_ReturnsCapitalized() {
        // Используем ReflectionTestUtils для вызова приватного метода
        String result = (String) ReflectionTestUtils.invokeMethod(userKeycloakService, "upperCaseFirstLetter", "john");

        // Assert
        assertEquals("John", result);
    }

    @Test
    void upperCaseFirstLetter_WithEmptyString_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                ReflectionTestUtils.invokeMethod(userKeycloakService, "upperCaseFirstLetter", ""));
    }

    @Test
    void upperCaseFirstLetter_WithNullString_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                ReflectionTestUtils.invokeMethod(userKeycloakService, "upperCaseFirstLetter", new Object[]{null}));
    }

    @Test
    void setRealm_WithValidIssuerUri_SetsRealmCorrectly() {
        // Arrange
        UserKeycloakService service = new UserKeycloakService(keycloakBuilderProvider, jwtDecoder);
        ReflectionTestUtils.setField(service, "issuerUri", issuerUri);

        // Act
        service.setRealm();

        // Assert
        String actualRealm = (String) ReflectionTestUtils.getField(service, "realm");
        assertEquals(realm, actualRealm);
    }

    // Тесты, которые требуют моки Keycloak
    @Test
    void generateJwtToken_WithInvalidCredentials_ReturnsEmpty() {
        // Arrange
        when(keycloakBuilderProvider.getObject()).thenReturn(keycloakBuilder);
        when(keycloakBuilder.grantType(any())).thenReturn(keycloakBuilder);
        when(keycloakBuilder.password(anyString())).thenReturn(keycloakBuilder);
        when(keycloakBuilder.username(anyString())).thenReturn(keycloakBuilder);
        when(keycloakBuilder.build()).thenThrow(new RuntimeException("Invalid credentials"));

        // Act
        Optional<Jwt> result = userKeycloakService.generateJwtToken(username, "wrongpassword");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void generateJwtToken_WithValidCredentials_ReturnsJwt() {
        // Arrange
        String tokenString = "test-token";

        when(keycloakBuilderProvider.getObject()).thenReturn(keycloakBuilder);
        when(keycloakBuilder.grantType(any())).thenReturn(keycloakBuilder);
        when(keycloakBuilder.password(anyString())).thenReturn(keycloakBuilder);
        when(keycloakBuilder.username(anyString())).thenReturn(keycloakBuilder);

        Keycloak keycloak = mock(Keycloak.class);
        when(keycloakBuilder.build()).thenReturn(keycloak);

        TokenManager tokenManager = mock(TokenManager.class);
        when(keycloak.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessTokenString()).thenReturn(tokenString);

        when(jwtDecoder.decode(tokenString)).thenReturn(jwt);

        // Act
        Optional<Jwt> result = userKeycloakService.generateJwtToken(username, "password");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(jwt, result.get());
    }

    @Test
    void getUser_WithEmptyUsername_ReturnsEmpty() {
        // Act
        Optional<UserRepresentation> result = userKeycloakService.getUser("");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void createUser_WhenCreationFails_ThrowsException() {
        // Arrange
        // Создаем отдельный мок Response для этого теста
        Response responseMock = mock(Response.class);

        // Настраиваем keycloakClientInstance для этого конкретного теста
        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(responseMock);
        when(responseMock.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());

        // Не нужно мокать getStatusInfo(), так как он не используется в вашем коде
        // Response.StatusType statusType = mock(Response.StatusType.class);
        // when(responseMock.getStatusInfo()).thenReturn(statusType);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                userKeycloakService.createUser(username, "John", "Doe", email, 123L, KeycloakRole.ROLE_CUSTOMER));
        verify(responseMock).close();
    }

    @Test
    void setUserPassword_WithValidData_ReturnsTrue() {
        // Arrange
        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);

        String password = "newPassword123";

        // Act
        boolean result = userKeycloakService.setUserPassword(userId, password);

        // Assert
        assertTrue(result);
        verify(userResource).resetPassword(any(CredentialRepresentation.class));
    }

    @Test
    void setUserPassword_WhenExceptionOccurs_ReturnsFalse() {
        // Arrange
        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        doThrow(new RuntimeException("Error")).when(userResource).resetPassword(any(CredentialRepresentation.class));

        // Act
        boolean result = userKeycloakService.setUserPassword(userId, "password");

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteUser_WithValidUserId_ReturnsTrue() {
        // Arrange
        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.delete(userId)).thenReturn(response);

        // Act
        boolean result = userKeycloakService.deleteUser(userId);

        // Assert
        assertTrue(result);
        verify(usersResource).delete(userId);
    }

    @Test
    void updateUserData_WithValidData_ReturnsTrue() {
        // Arrange
        UpdateUserRequestDTO userDTO = new UpdateUserRequestDTO();
        userDTO.setFirstName("NewJohn");
        userDTO.setLastName("NewDoe");
        userDTO.setEmail("new@example.com");

        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);

        UserRepresentation userRep = new UserRepresentation();
        userRep.setFirstName("OldJohn");
        userRep.setLastName("OldDoe");
        userRep.setEmail("old@example.com");
        when(userResource.toRepresentation()).thenReturn


                (userRep);

        // Act
        boolean result = userKeycloakService.updateUserData(userId, userDTO);

        // Assert
        assertTrue(result);
        verify(userResource).update(userRep);
    }

    @Test
    void getUser_WithExistingUsername_ReturnsUser() {
        // Arrange
        List<UserRepresentation> users = new ArrayList<>();
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        users.add(user);

        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list(0, 100)).thenReturn(users);

        // Act
        Optional<UserRepresentation> result = userKeycloakService.getUser(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
    }

    @Test
    void userExists_WithExistingEmail_ReturnsEmailExists() {
        // Arrange
        List<UserRepresentation> users = new ArrayList<>();
        UserRepresentation user = new UserRepresentation();
        user.setEmail(email);
        user.setUsername("otheruser");
        user.setId("user123");
        users.add(user);

        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list(anyInt(), anyInt())).thenReturn(users);

        // Act
        UserExistenceStatus result = userKeycloakService.userExists(email, null, null);

        // Assert
        assertEquals(UserExistenceStatus.EMAIL_EXISTS, result);
    }

    @Test
    void collectKeycloakUserInfo_WithValidUserId_ReturnsUserInfo() {
        // Arrange
        UserRepresentation userRep = new UserRepresentation();
        userRep.setEmail(email);
        userRep.setFirstName("John");
        userRep.setLastName("Doe");
        userRep.setUsername(username);

        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);

        // Act
        Optional<UserResponseDTO> result = userKeycloakService.collectKeycloakUserInfo(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
        assertEquals(username, result.get().getNickName());
    }

    @Test
    void userEmailIsChanged_WithDifferentEmail_ReturnsTrue() {
        // Arrange
        String newEmail = "new@example.com";
        UserRepresentation userRep = new UserRepresentation();
        userRep.setEmail(email);

        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);

        // Act
        boolean result = userKeycloakService.userEmailIsChanged(userId, newEmail);

        // Assert
        assertTrue(result);
    }

    @Test
    void logout_WithValidUserId_ReturnsTrue() {
        // Arrange
        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);

        // Act
        boolean result = userKeycloakService.logout(userId);

        // Assert
        assertTrue(result);
        verify(userResource).logout();
    }

    @Test
    void getUserFullNames_WithValidUserIds_ReturnsFullNames() {
        // Arrange
        List<String> userIds = Arrays.asList("user1", "user2");

        UserRepresentation user1 = new UserRepresentation();
        user1.setFirstName("John");
        user1.setLastName("Doe");

        UserRepresentation user2 = new UserRepresentation();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");

        ReflectionTestUtils.setField(userKeycloakService, "keycloakClientInstance", keycloakClientInstance);
        when(keycloakClientInstance.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        when(usersResource.get("user1")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(user1);

        UserResource userResource2 = mock(UserResource.class);
        when(usersResource.get("user2")).thenReturn(userResource2);
        when(userResource2.toRepresentation()).thenReturn(user2);

        // Act
        List<UserFullNameDto> result = userKeycloakService.getUserFullNames(userIds);

        // Assert
        assertEquals(2, result.size());
    }
}