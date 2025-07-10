package com.example.managerapp.service;

import com.example.managerapp.dto.EditUserDTO;
import com.example.managerapp.dto.GetUserDTO;
import com.example.managerapp.dto.RegistrationUserDTO;
import com.example.managerapp.entity.MyUser;
import com.example.managerapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class UserService {

    private Keycloak keycloak;      // клиент, который авторизируется в keycloak и позволяет делать запросы от имени админа. Позволяет создать пользователя, проверить, существует ли он и т.д.

    @Autowired
    private UserRepository userRepository;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-uri}")
    private String serverUrl;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String client_id;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String client_secret;



    @PostConstruct
    public void init() {
        this.keycloak =  KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(client_id)
                .clientSecret(client_secret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();



    }

    public boolean createUser(RegistrationUserDTO userDTO){
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getNickName());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);

        Response response = keycloak.realm(realm).users().create(user);
        if(response.getStatus() != 201){
            return false;
        }

        String userID = CreatedResponseUtil.getCreatedId(response);
        CredentialRepresentation password = new CredentialRepresentation();
        password.setTemporary(false);
        password.setType(CredentialRepresentation.PASSWORD);
        password.setValue(userDTO.getPassword());

        keycloak.realm(realm).users().get(userID).resetPassword(password);

        MyUser myUser = new MyUser();
        myUser.setKeycloakID(userID);
        userRepository.save(myUser);

        return true;

    }
    public void updateUserData(String userId, EditUserDTO userDTO) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);

        UserRepresentation user = userResource.toRepresentation();

        if(userDTO.getFirstName() != null){
            user.setFirstName(userDTO.getFirstName());
        }
        if(userDTO.getLastName() != null){
            user.setLastName(userDTO.getLastName());
        }
        if(userDTO.getEmail() != null){
            user.setEmail(userDTO.getEmail());
            user.setEmailVerified(true);
        }

       if(userDTO.getPassword() != null){
           updatePassword(userId, userDTO.getPassword());
       }

       userResource.update(user);


    }
    public void updatePassword(String userId, String newPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);

        keycloak.realm(realm)
                .users()
                .get(userId)
                .resetPassword(credential);
    }


    public int UserExists(String email, String username, String excludedUser){   //если возвращает 0 - НЕ СУЩЕСТВУЕТ, 1 - ЕСТЬ ПО EMAIL, 2 - ЕСТЬ ПО НИКНЕЙМУ

        if(email==null&&username==null) return 0;
        int first = 0;
        int maxResults = 100;

        while (true) {
            List<UserRepresentation> users = keycloak.realm(realm).users().list(first, maxResults);

            if (users.isEmpty()) {
                break;
            }

            for (UserRepresentation user : users) {

                if(excludedUser!=null && user.getId().equals(excludedUser)) continue;

                if (email!=null&&email.equalsIgnoreCase(user.getEmail())) return 1;
                if (username!=null&&username.equalsIgnoreCase(user.getUsername())) return 2;
            }

            first += maxResults;
        }

        return 0;

    }

    public Optional<GetUserDTO> collectUserInfo(OAuth2AuthenticationToken authentication) {
        if (authentication == null) return Optional.empty();

        String userID = getUserID(authentication);
        if (userID == null) return Optional.empty();

        UserResource userResource = keycloak.realm(realm).users().get(userID);

        try {
            UserRepresentation userRep = userResource.toRepresentation();

            GetUserDTO user = new GetUserDTO();
            user.setEmail(userRep.getEmail());
            user.setFirstName(userRep.getFirstName());
            user.setLastName(userRep.getLastName());
            user.setNickName(userRep.getUsername());

            Optional<MyUser> userOptional = userRepository.findByKeycloakID(userID);
            userOptional.ifPresent(myUser -> {

                        user.setId(myUser.getId());
                        user.setAvatarFileName(myUser.getAvatarFileName());

                     }
                   );

            return Optional.of(user);

        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    public MyUser getMyUserFromBD(String keycloakID) {
        if (keycloakID == null) throw new NotFoundException();
        Optional<MyUser> user = userRepository.findByKeycloakID(keycloakID);
        if (user.isEmpty()) throw new NotFoundException();
        return user.get();
    }

    public Optional<MyUser> getMyUserFromBD(Long userId) {
        return userRepository.findById(userId);
    }



    public String getUserID(OAuth2AuthenticationToken authentication){
        OAuth2User user = authentication.getPrincipal();

        return user.getAttribute("sub");
    }

    public boolean isEmailChanged(OAuth2AuthenticationToken authentication, String newEmail){
        Optional<GetUserDTO> userOptional = collectUserInfo(authentication);
        return !newEmail.equalsIgnoreCase(userOptional.get().getEmail());


    }

    public void saveMyUserToBD(MyUser user){
        userRepository.save(user);
    }





}
