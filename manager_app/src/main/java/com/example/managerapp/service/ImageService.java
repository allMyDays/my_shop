package com.example.managerapp.service;

import com.example.managerapp.entity.MyUser;
import com.example.managerapp.entity.enums.MinIO_bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final MinioService minioService;

    private final UserService userService;




    public void saveUserAvatar(MultipartFile file, OAuth2AuthenticationToken authenticationToken) throws IOException {



        String newFileName =  minioService.saveFile(file, MinIO_bucket.users);

        MyUser myUser = userService.getMyUserFromPostgres(authenticationToken);
            if(myUser.getAvatarFileName()!=null){
                minioService.deleteFile(myUser.getAvatarFileName(), MinIO_bucket.users);
            }
            myUser.setAvatarFileName(newFileName);
            userService.saveMyUserToPostgres(myUser);
    }

    public void deleteUserAvatar(OAuth2AuthenticationToken authenticationToken) {

        MyUser myUser = userService.getMyUserFromPostgres(authenticationToken);
            if(myUser.getAvatarFileName()!=null){
            minioService.deleteFile(myUser.getAvatarFileName(),MinIO_bucket.users);
            myUser.setAvatarFileName(null);
            userService.saveMyUserToPostgres(myUser);
            }
    }






















}
