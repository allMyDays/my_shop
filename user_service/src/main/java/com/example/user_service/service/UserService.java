package com.example.user_service.service;


import com.example.common.client.kafka.EmailKafkaClient;
import com.example.common.dto.user.CreateUserRequestDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.common.dto.user.UserResponseDTO;
import com.example.user_service.entity.MyUser;
import com.example.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.example.user_service.enumeration.RedisSubKeys.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserKeycloakService userKeycloakService;

    private final EmailKafkaClient emailKafkaClient;

    private final RedisService redisService;

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
           newUserEntity.setKeycloakID(newKeycloakId);
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

        UserNotFoundException exception = new UserNotFoundException();

        UserResponseDTO userResponseDTO = userKeycloakService.collectKeycloakUserInfo(userKeycloakId).orElseThrow(()->exception);

        MyUser userEntity = getUserEntity(userKeycloakId);

        userResponseDTO.setId(userEntity.getId());

        userResponseDTO.setAvatarFileName(userEntity.getAvatarFileName());

        return userResponseDTO;
    }

    public UserResponseDTO collectCommonUserInfo(Long userEntityId) throws UserNotFoundException {
        MyUser userEntity = getUserEntity(userEntityId);
        return collectCommonUserInfo(userEntity.getKeycloakID());
    }



    public void updateUserAvatar(Long userId, String fileName){

        MyUser userEntity = getUserEntity(userId);
        userEntity.setAvatarFileName(fileName);
        userRepository.save(userEntity);

    }

    public void deleteUserAvatar(Long userId){

        MyUser userEntity = getUserEntity(userId);
        userEntity.setAvatarFileName(null);
        userRepository.save(userEntity);
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

    public MyUser getUserEntity(String keycloakID) {

        NotFoundException notFoundException = new NotFoundException("User not found");

        if (keycloakID == null) throw notFoundException;

        return userRepository.findByKeycloakID(keycloakID)
                .orElseThrow(()->notFoundException);

    }

    public MyUser getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException("User not found"));
    }

    public Optional<MyUser> getUserOptionalEntity(Long userId) {
        return userRepository.findById(userId);
    }

    public MyUser getUserEntityReference(Long userId){
        return userRepository.getReferenceById(userId);
    }

    @Transactional
    public MyUser saveUserEntity(MyUser user) throws Exception{

        MyUser savedUser = userRepository.save(user);
        userRepository.flush();
        return savedUser;
    }











}
