package com.example.user_service.service;


import com.example.common.client.kafka.EmailKafkaClient;
import com.example.common.client.kafka.MediaKafkaClient;
import com.example.common.dto.user.rest.CreateUserRequestDTO;
import com.example.common.enumeration.media_service.BucketEnum;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.user.rest.UserResponseDTO;
import com.example.user_service.entity.MyUser;
import com.example.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static com.example.common.service.CommonMediaService.validateImages;
import static com.example.user_service.enumeration.RedisSubKeys.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserKeycloakService userKeycloakService;

    private final EmailKafkaClient emailKafkaClient;

    private final RedisService redisService;

    private final MediaKafkaClient mediaKafkaClient;

    @Autowired
    @Lazy
    private UserService userService;

    public boolean createCommonUser(CreateUserRequestDTO userDTO) {
        MyUser newUserEntity = null;
       String newKeycloakId = null;
       try{
           newUserEntity = userService.saveUserEntity(new MyUser());
           newKeycloakId = userKeycloakService.createUser(userDTO.getNickName(),userDTO.getFirstName(),userDTO.getLastName(),userDTO.getEmail(),newUserEntity.getId());

           userKeycloakService.setUserPassword(newKeycloakId, userDTO.getPassword());
           newUserEntity.setKeycloakId(newKeycloakId);
           userService.saveUserEntity(newUserEntity);
           return true;

       }catch (Exception e){
           if(newKeycloakId != null){
               userKeycloakService.deleteUser(newKeycloakId);
           } if(newUserEntity!=null){
               userRepository.delete(newUserEntity);
           } return false;
       }
    }

    public UserResponseDTO collectCommonUserInfo(String userKeycloakId) throws UserNotFoundException {

        UserNotFoundException notFoundException = new UserNotFoundException();

        UserResponseDTO userResponseDTO = userKeycloakService.collectKeycloakUserInfo(userKeycloakId).orElseThrow(()->notFoundException);

        MyUser userEntity = getUserEntity(userKeycloakId);

        userResponseDTO.setId(userEntity.getId());

        userResponseDTO.setAvatarFileName(userEntity.getAvatarFileName());

        return userResponseDTO;
    }

    public UserResponseDTO collectCommonUserInfo(Long userEntityId) throws UserNotFoundException {
        MyUser userEntity = getUserEntity(userEntityId);
        return collectCommonUserInfo(userEntity.getKeycloakId());
    }



    public void sendUploadUserAvatarRequest(MultipartFile image, Long userId) {

        validateImages(List.of(image));

        MyUser userEntity = getUserEntity(userId);

        String requestKey = UUID.randomUUID().toString();

        redisService.save(KAFKA_UPLOAD_AVATAR+":"+requestKey,userEntity.getId().toString());

        mediaKafkaClient.sendSavingMediaRequest(List.of(image), BucketEnum.users, requestKey);
    }

    public void saveUserAvatar(String fileName, Long userId){
        MyUser userEntity = getUserEntity(userId);
        userEntity.setAvatarFileName(fileName);
        userRepository.save(userEntity);

    }



    public void deleteUserAvatar(Long userId){

        MyUser userEntity = getUserEntity(userId);
        String avatarFileName = userEntity.getAvatarFileName();

        if(avatarFileName != null){
         userEntity.setAvatarFileName(null);
         userRepository.save(userEntity);
         mediaKafkaClient.deleteMedia(List.of(avatarFileName));
        }


    }


    public boolean userEmailIsVerifiedOrSendCodeOtherwise(String userEmail) throws MailSendException {

        if(redisService.get(CONFIRMED_EMAIL +":"+ userEmail)!=null){
            redisService.delete(List.of(
                     CONFIRMED_EMAIL +":"+ userEmail
                    ,CONFIRMING_EMAIL+":"+userEmail
                    ,CONFIRMING_EMAIL_ATTEMPT_NUMBER+":"+userEmail));
            return true;
        }

        int newCode = 100_000 + new Random().nextInt(900_000);

        emailKafkaClient.sendSimpleMail(userEmail,"Подтверждение почтового ящика", "Введите в поле ввода этот код: \n"+newCode);

        redisService.saveTemp(CONFIRMING_EMAIL+":"+userEmail, Integer.toString(newCode),3600);

        return false;
    }

    public MyUser getUserEntity(@NonNull String keycloakID) {

        NotFoundException notFoundException = new NotFoundException("User not found");


        return userRepository.findByKeycloakId(keycloakID)
                .orElseThrow(()->notFoundException);

    }

    public MyUser getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public Optional<MyUser> getUserOptionalEntity(Long userId) {
        return userRepository.findById(userId);
    }


    @Transactional
    public MyUser saveUserEntity(MyUser user) throws Exception{

        MyUser savedUser = userRepository.save(user);
        userRepository.flush();
        return savedUser;
    }

    public MyUser getOrCreateMyUser(@NonNull Long userId, @NonNull String userKeycloakId){

        Optional<MyUser> userOptional = getUserOptionalEntity(userId);
        if(userOptional.isEmpty()){
            userRepository.insertUserNativeQuery(userId,userKeycloakId);
        } return getUserEntity(userId);

    }











}
