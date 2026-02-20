package com.example.user_service.service;


import com.example.common.client.grpc.MediaGrpcClient;
import com.example.common.client.kafka.EmailKafkaClient;
import com.example.common.client.kafka.MediaKafkaClient;
import com.example.common.mapper.MediaMapper;
import com.example.user_service.dto.CreateUserRequestDTO;
import com.example.common.enumeration.media.BucketEnum;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.user.rest.UserResponseDTO;
import com.example.user_service.entity.MyUser;
import com.example.common.enumeration.user.KeycloakRole;
import com.example.user_service.enumeration.UserCreationInnerStatus;
import com.example.user_service.exception.KeycloakException;
import com.example.user_service.exception.KeycloakUserAlreadyExistsException;
import com.example.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.example.user_service.enumeration.RedisSubKeys.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserKeycloakService userKeycloakService;

    private final EmailKafkaClient emailKafkaClient;

    private final RedisService redisService;

    private final MediaKafkaClient mediaKafkaClient;

    private final MediaGrpcClient mediaGrpcClient;

    private final MediaMapper mediaMapper;

    @Autowired
    @Lazy
    private UserService selfLink;

    public UserCreationInnerStatus createCommonUser(@NonNull CreateUserRequestDTO userDTO, KeycloakRole keycloakRole) {
        MyUser newUserEntity = null;
       String newKeycloakId = null;
       try{
           newUserEntity = selfLink.saveUserEntity(new MyUser());
           newKeycloakId = userKeycloakService.createKeycloakUser(
                   userDTO.getNickName(),
                   userDTO.getFirstName(),
                   userDTO.getLastName(),
                   userDTO.getEmail(),
                   newUserEntity.getId(),
                   keycloakRole);

           userKeycloakService.setPasswordForUser(newKeycloakId, userDTO.getPassword());
           newUserEntity.setKeycloakId(newKeycloakId);
           selfLink.saveUserEntity(newUserEntity);
           return UserCreationInnerStatus.SUCCESS;

       }catch (KeycloakUserAlreadyExistsException e){
           rollBackUser(newUserEntity, newKeycloakId);
           return UserCreationInnerStatus.USER_ALREADY_EXISTS;

       }catch (KeycloakException e){
           rollBackUser(newUserEntity, newKeycloakId);
           return UserCreationInnerStatus.KEYCLOAK_ERROR;
       }catch (Exception e){
           rollBackUser(newUserEntity, newKeycloakId);
           return UserCreationInnerStatus.INTERNAL_ERROR;

       }
    }
    private void rollBackUser(MyUser userEntity, String keycloakId){
        if(keycloakId!=null){
            try{
                userKeycloakService.deleteUser(keycloakId);
            }catch (Exception e){
                log.error("Failed to rollback keycloak user {}", keycloakId, e);

            }
            if(userEntity!=null){
                try{
                    userRepository.delete(userEntity);
                }catch (Exception e){
                    log.error("Failed to rollback local user {}", userEntity.getId(), e);

                }
            }
        }
    }









    @Transactional
    public UserResponseDTO collectCommonUserInfo(@NonNull String userKeycloakId) throws UserNotFoundException {


        UserResponseDTO userResponseDTO = userKeycloakService.collectKeycloakUserInfo(userKeycloakId)
                .orElseThrow(UserNotFoundException::new);

        MyUser userEntity = getUserEntity(userKeycloakId);

        userResponseDTO.setId(userEntity.getId());

        userResponseDTO.setAvatarFileName(userEntity.getAvatarFileName());

        return userResponseDTO;
    }

    public UserResponseDTO collectCommonUserInfo(long userEntityId) throws UserNotFoundException {
        MyUser userEntity = getUserEntity(userEntityId);
        return selfLink.collectCommonUserInfo(userEntity.getKeycloakId());
    }

    public void saveUserAvatar(@NonNull MultipartFile image, long userId){

        MyUser userEntity = getUserEntity(userId);

        List<String> newFileNames = mediaGrpcClient.uploadPhotos(mediaMapper.toPhotoDataDtoList(List.of(image)), BucketEnum.users);
        if(newFileNames.isEmpty()){
            throw new RuntimeException("Failed to upload avatar: generated avatar name absents");
        }

        userEntity.setAvatarFileName(newFileNames.get(0));
        userRepository.save(userEntity);

    }

    public void deleteUserAvatar(long userId){

        MyUser userEntity = getUserEntity(userId);
        String avatarFileName = userEntity.getAvatarFileName();

        if(avatarFileName != null){
         userEntity.setAvatarFileName(null);
         userRepository.save(userEntity);
         mediaKafkaClient.deleteMedia(List.of(avatarFileName));
        }


    }


    public boolean userEmailIsVerifiedOrSendCodeOtherwise(@NonNull String userEmail) throws MailSendException {

        if(redisService.get(CONFIRMED_EMAIL +":"+ userEmail,true).isPresent()){
            redisService.delete(List.of(
                    CONFIRMING_EMAIL+":"+userEmail
                    ,CONFIRMING_EMAIL_ATTEMPT_NUMBER+":"+userEmail));
            return true;
        }

        int newCode = 100_000 + new Random().nextInt(900_000);

        emailKafkaClient.sendSimpleMail(userEmail,"Подтверждение почтового ящика", "Введите в поле ввода этот код: \n"+newCode);

        redisService.saveTemp(CONFIRMING_EMAIL+":"+userEmail, Integer.toString(newCode),3600);

        return false;
    }

    public MyUser getUserEntity(@NonNull String keycloakID) {

        return userRepository.findByKeycloakId(keycloakID)
                .orElseThrow(()->new NotFoundException("User not found"));

    }

    public MyUser getUserEntity(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public Optional<MyUser> getUserOptionalEntity(long userId) {
        return userRepository.findById(userId);
    }


    @Transactional
    public MyUser saveUserEntity(MyUser user) {

        MyUser savedUser = userRepository.save(user);
        userRepository.flush();
        return savedUser;
    }

    public MyUser getOrCreateMyUser(long userId, @NonNull String userKeycloakId){

        Optional<MyUser> userOptional = getUserOptionalEntity(userId);
        if(userOptional.isEmpty()){
            userRepository.insertUserNativeQuery(userId,userKeycloakId);
        } return getUserEntity(userId);

    }

}
