package com.example.user_service.service;

import com.example.common.dto.user.UpdateUserRequestDTO;
import com.example.common.dto.user.UserResponseDTO;
import com.example.common.enumeration.grpc.UserExistenceStatus;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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


    public String createUser(String nickName, String firstName, String lastName, String email, Long userEntityId) throws Exception {
        Response response = null;
        Exception exception = new RuntimeException("Error creating user");
      try {
          UserRepresentation userRep = new UserRepresentation();
          userRep.setUsername(nickName);
          userRep.setFirstName(firstName);
          userRep.setLastName(lastName);
          userRep.setEmail(email);
          userRep.setEnabled(true);
          userRep.setEmailVerified(true);


          Map<String, List<String>> attributes = new HashMap<>();
          attributes.put(MY_USER_ID_KEY_KEYCLOAK, List.of(userEntityId.toString()));
          userRep.setAttributes(attributes);
          response = keycloakClientInstance.realm(realm).users().create(userRep);

          if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
              throw exception;
          }
          return CreatedResponseUtil.getCreatedId(response);

      }catch(Exception e){
          throw exception;
      } finally {
          if(response!=null) response.close();
      }
    }

    public boolean setUserPassword(String userId, String newPassword) {
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

    public boolean deleteUser(String userId) {
        try(Response response = keycloakClientInstance.realm(realm).users().delete(userId)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean updateUserData(String userKeycloakId, UpdateUserRequestDTO userDTO){
        try{

            UserResource userResource = keycloakClientInstance.realm(realm).users().get(userKeycloakId);

            UserRepresentation userRep = userResource.toRepresentation();

            boolean updated = false;

            if(userDTO.getFirstName() != null){
                userRep.setFirstName(userDTO.getFirstName());
                updated = true;
            }
            if(userDTO.getLastName() != null){
                userRep.setLastName(userDTO.getLastName());
                updated = true;
            }
            if(userDTO.getEmail() != null){
                userRep.setEmail(userDTO.getEmail());
                userRep.setEmailVerified(true);
                updated = true;
            }

            if(userDTO.getPassword() != null){
                setUserPassword(userKeycloakId, userDTO.getPassword());
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

    public Optional<UserRepresentation > getUser(String username){

        if(username==null||username.isEmpty()) return Optional.empty();

        int first = 0;
        int maxResults = 100;

        while (true) {
            List<UserRepresentation> users = keycloakClientInstance.realm(realm).users().list(first, maxResults);

            if (users.isEmpty()) {
                break;
            }

            for (UserRepresentation user : users) {
                if (username.equalsIgnoreCase(user.getUsername())) return Optional.of(user);
            }

            first += maxResults;
        }

        return Optional.empty();

    }


    public UserExistenceStatus userExists(String email, String username, String excludedUser){

        if(email==null&&username==null) return UserExistenceStatus.NOT_EXISTS;

        int first = 0;
        int maxResults = 100;

        while (true) {
            List<UserRepresentation> users = keycloakClientInstance.realm(realm).users().list(first, maxResults);

            if (users.isEmpty()) {
                break;
            }

            for (UserRepresentation user : users) {

                if(excludedUser!=null && user.getId().equals(excludedUser)) continue;

                if (email!=null&&email.equalsIgnoreCase(user.getEmail())) return UserExistenceStatus.EMAIL_EXISTS;
                if (username!=null&&username.equalsIgnoreCase(user.getUsername())) return UserExistenceStatus.USERNAME_EXISTS;
            }

            first += maxResults;
        }

        return UserExistenceStatus.NOT_EXISTS;

    }
    public String getUserRealm(Jwt jwt){
        String issuer = jwt.getClaimAsString("iss");
        return issuer.substring(issuer.lastIndexOf("/")+1);
    }

    public boolean userEmailIsChanged(String userKeycloakID, String newEmail){

        UserRepresentation userRep = keycloakClientInstance.realm(realm).users().get(userKeycloakID).toRepresentation();

        return !newEmail.equalsIgnoreCase(userRep.getEmail());

    }

    public Optional<UserResponseDTO> collectKeycloakUserInfo(String userKeycloakID){

        UserResource userResource = keycloakClientInstance.realm(realm).users().get(userKeycloakID);


        UserRepresentation userRep = userResource.toRepresentation();

        UserResponseDTO userDTO = new UserResponseDTO();
        userDTO.setEmail(userRep.getEmail());
        userDTO.setFirstName(userRep.getFirstName());
        userDTO.setLastName(userRep.getLastName());
        userDTO.setNickName(userRep.getUsername());

        return Optional.of(userDTO);


    }

    public static String extractRealm(String issuerUri){

        if(issuerUri==null) throw new IllegalArgumentException("Issuer URI is null");
        if(issuerUri.endsWith("/")) issuerUri = issuerUri.substring(0, issuerUri.length()-1);

        String[] splitIssuerUri  = issuerUri.split("/");
        String realm = splitIssuerUri[splitIssuerUri.length - 1];
        if(realm.isEmpty()) throw new IllegalArgumentException("Issuer URI is empty");
        return realm;

    }

    public static String extractServerUri(String issuerUri){
        if(issuerUri==null) throw new IllegalArgumentException("Issuer URI is null");
        String[] splitIssuerUri  = issuerUri.split("/");
        if(splitIssuerUri.length<3||!splitIssuerUri[0].matches("http(s)?:")) throw new IllegalArgumentException("Issuer URI is incorrect");

        return splitIssuerUri[0]+"//"+splitIssuerUri[2];

    }

    public Optional<Jwt> generateJwtToken(String nickName, String password){
        try(Keycloak keycloak = keycloakBuilderProvider
                    .getObject()
                    .grantType(PASSWORD)
                    .password(password)
                    .username(nickName)
                    .build()) {

            Jwt jwt = jwtDecoder.decode(keycloak.tokenManager().getAccessTokenString());
            return Optional.of(jwt);

        }catch (Exception e){
            return Optional.empty();
        }
    }


    public boolean logout(String userKeycloakID){

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












}
