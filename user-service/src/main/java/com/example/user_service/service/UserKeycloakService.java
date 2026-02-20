package com.example.user_service.service;

import com.example.user_service.dto.UpdateUserRequestDTO;
import com.example.common.dto.user.rest.UserResponseDTO;
import com.example.common.enumeration.user.UserExistenceStatus;
import com.example.user_service.dto.UserFullNameDto;
import com.example.common.enumeration.user.KeycloakRole;
import com.example.user_service.exception.KeycloakException;
import com.example.user_service.exception.KeycloakUserAlreadyExistsException;
import io.lettuce.core.dynamic.annotation.Key;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.common.service.CommonUserService.MY_USER_ID_KEY_KEYCLOAK;
import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;
import static org.keycloak.OAuth2Constants.PASSWORD;

@Service
public class UserKeycloakService {

    private final ObjectProvider<KeycloakBuilder> keycloakBuilderProvider;

    private Keycloak keycloakClientInstance;


    private String realm;

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    private final JwtDecoder jwtDecoder;

    String SUPPORT_AGENT_ROLE_NAME = "ROLE_AGENT";


    @Autowired
    public UserKeycloakService(ObjectProvider<KeycloakBuilder> keycloakBuilderProvider, JwtDecoder jwtDecoder){
        this.keycloakBuilderProvider = keycloakBuilderProvider;
        this.jwtDecoder = jwtDecoder;
    }

    @PostConstruct
    public void setRealm() {
        realm = extractRealm(issuerUri);
    }

    @PostConstruct
    public void setKeycloakClientInstance() {
        keycloakClientInstance = keycloakBuilderProvider
                .getObject()
                .grantType(CLIENT_CREDENTIALS)
                .build();

    }


    public String createKeycloakUser(
            @NonNull String nickName,
            @NonNull String firstName,
            @NonNull String lastName,
            @NonNull String email,
            long userEntityId,
            KeycloakRole keycloakRole) {
        Response response = null;
      try {
          UserRepresentation userRep = new UserRepresentation();
          userRep.setUsername(nickName);
          userRep.setFirstName(upperCaseFirstLetter(firstName));
          userRep.setLastName(upperCaseFirstLetter(lastName));
          userRep.setEmail(email);
          userRep.setEnabled(true);
          userRep.setEmailVerified(true);


          Map<String, List<String>> attributes = new HashMap<>();
          attributes.put(MY_USER_ID_KEY_KEYCLOAK, List.of(String.valueOf(userEntityId)));
          userRep.setAttributes(attributes);
          response = keycloakClientInstance
                  .realm(realm)
                  .users()
                  .create(userRep);

          if(response.getStatus()==409){
              throw new KeycloakUserAlreadyExistsException(null);
          }

          if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
              throw new KeycloakException(new RuntimeException("Keycloak error: "+response.getStatus()));
          }
          String newUserId =  CreatedResponseUtil.getCreatedId(response);
          setRoleForUser(newUserId, keycloakRole);
          return newUserId;
      } finally {
          if(response!=null) response.close();
      }
    }

    public void setRoleForUser(@NonNull String userId, @NonNull KeycloakRole keycloakRole) {

        RoleRepresentation role = keycloakClientInstance
                .realm(realm)
                .roles()
                .get(keycloakRole.name())
                .toRepresentation();

        keycloakClientInstance
                .realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(role));
    }

    public boolean setPasswordForUser(@NonNull String userId, @NonNull String newPassword) {
        try{
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            keycloakClientInstance.realm(realm)
                    .users()
                    .get(userId)
                    .resetPassword(credential);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public boolean deleteUser(@NonNull String userId) {
        try(Response response = keycloakClientInstance.realm(realm).users().delete(userId)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean updateUserData(@NonNull String userKeycloakId, String firstName, String lastName, String email){
        try{

            UserResource userResource = keycloakClientInstance
                    .realm(realm)
                    .users()
                    .get(userKeycloakId);

            UserRepresentation userRep = userResource.toRepresentation();

            boolean updated = false;

            if(firstName != null){
                userRep.setFirstName(upperCaseFirstLetter(firstName));
                updated = true;
            }
            if(lastName != null){
                userRep.setLastName(upperCaseFirstLetter(lastName));
                updated = true;
            }
            if(email!= null){
                userRep.setEmail(email);
                userRep.setEmailVerified(true);
                updated = true;
            }
            if(updated){
                userResource.update(userRep);
            }

            return true;

        } catch(Exception e){
            return false;

        }
    }

    public Optional<UserRepresentation> getKeycloakUser(@NonNull String username){

        if(username.isEmpty()) return Optional.empty();


        List<UserRepresentation> users =
                keycloakClientInstance.realm(realm)
                        .users()
                        .search(username, true);

        return users.isEmpty()?Optional.empty():Optional.of(users.get(0));

    }

    public UserExistenceStatus userExists(
            String email,
            String username,
            String excludedUserId
    ) {
        if (email != null) {
            List<UserRepresentation> users =
                    keycloakClientInstance.realm(realm)
                            .users()
                            .searchByEmail(email, true);

            if (users.stream().anyMatch(u -> !u.getId().equals(excludedUserId))) {
                return UserExistenceStatus.EMAIL_EXISTS;
            }
        }

        if (username != null) {
            List<UserRepresentation> users =
                    keycloakClientInstance.realm(realm)
                            .users()
                            .search(username, true);

            if (users.stream().anyMatch(u -> !u.getId().equals(excludedUserId))) {
                return UserExistenceStatus.USERNAME_EXISTS;
            }
        }

        return UserExistenceStatus.NOT_EXISTS;
    }

    public String getUserRealm(@NonNull Jwt jwt){
        String issuer = jwt.getClaimAsString("iss");
        return issuer.substring(issuer.lastIndexOf("/")+1);
    }

    public boolean userEmailIsChanged(@NonNull String userKeycloakID, @NonNull String newEmail){

        UserRepresentation userRep = keycloakClientInstance
                .realm(realm)
                .users()
                .get(userKeycloakID)
                .toRepresentation();

        return !newEmail.equalsIgnoreCase(userRep.getEmail());

    }

    public Optional<UserResponseDTO> collectKeycloakUserInfo(@NonNull String userKeycloakID){

        try{
        UserResource userResource = keycloakClientInstance.realm(realm).users().get(userKeycloakID);
        UserRepresentation userRep = userResource.toRepresentation();

        UserResponseDTO userDTO = new UserResponseDTO();
        userDTO.setEmail(userRep.getEmail());
        userDTO.setFirstName(userRep.getFirstName());
        userDTO.setLastName(userRep.getLastName());
        userDTO.setNickName(userRep.getUsername());
        return Optional.of(userDTO);

         }
        catch (Exception e) {
            return Optional.empty();
        }


    }

    public static String extractRealm(@NonNull String issuerUri){

        if(issuerUri.endsWith("/")) issuerUri = issuerUri.substring(0, issuerUri.length()-1);

        String[] splitIssuerUri  = issuerUri.split("/");
        String realm = splitIssuerUri[splitIssuerUri.length - 1];
        if(realm.isEmpty()) throw new IllegalArgumentException("Issuer URI is empty");
        return realm;

    }

    public static String extractServerUri(@NonNull String issuerUri){
        String[] splitIssuerUri  = issuerUri.split("/");
        if(splitIssuerUri.length<3||!splitIssuerUri[0].matches("http(s)?:")) throw new IllegalArgumentException("Issuer URI is incorrect");

        return splitIssuerUri[0]+"//"+splitIssuerUri[2];

    }

    public Jwt generateJwtToken(@NonNull String nickName,@NonNull String password){
        try(Keycloak keycloak = keycloakBuilderProvider
                    .getObject()
                    .grantType(PASSWORD)
                    .password(password)
                    .username(nickName)
                    .build()) {

            return jwtDecoder.decode(keycloak.tokenManager().getAccessTokenString());

        }catch (NotAuthorizedException e){
            throw new BadCredentialsException("Неверный логин или пароль.");
        }
    }


    public boolean logout(@NonNull String userKeycloakID){

       try {
           keycloakClientInstance
                   .realm(realm)
                   .users()
                   .get(userKeycloakID)
                   .logout();
            return true;
       }catch (Exception e){
           return false;
       }

    }

    public List<UserFullNameDto> getUserFullNames(@NonNull List<String> userKeycloakIDs){

        List<UserFullNameDto> userFullNameDTOs = new ArrayList<>();


        for(String userKeycloakID: userKeycloakIDs){
            try{
                UserRepresentation userResource = keycloakClientInstance.realm(realm)
                        .users()
                        .get(userKeycloakID)
                        .toRepresentation();

                userFullNameDTOs.add(new UserFullNameDto(userKeycloakID,userResource.getFirstName(), userResource.getLastName()));

         }catch (Exception e){
             continue;
          }
        } return userFullNameDTOs;
    }

    public String upperCaseFirstLetter(String input){
        if (input==null||input.isEmpty()) throw new IllegalArgumentException("Input is blank");
        return input.substring(0, 1).toUpperCase() + input.substring(1);

    }

}
