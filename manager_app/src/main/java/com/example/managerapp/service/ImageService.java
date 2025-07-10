package com.example.managerapp.service;

import com.example.managerapp.entity.MyUser;
import com.example.managerapp.entity.enums.MinIO_bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final MinioService minioService;

    private final UserService userService;




    public void saveUserAvatar(MultipartFile file, Long userId) throws IOException {

        String newFileName =  minioService.saveFile(file, MinIO_bucket.users);

        userService.getMyUserFromBD(userId).ifPresent(myUser -> {
            if(myUser.getAvatarFileName()!=null){
                minioService.deleteFile(myUser.getAvatarFileName(), MinIO_bucket.users);
            }
            myUser.setAvatarFileName(newFileName);
            userService.saveMyUserToBD(myUser);
        });
    }

    public void deleteUserAvatar(Long userId) {

        userService.getMyUserFromBD(userId).ifPresent(myUser -> {
            minioService.deleteFile(myUser.getAvatarFileName(),MinIO_bucket.users);
            myUser.setAvatarFileName(null);
            userService.saveMyUserToBD(myUser);
        });
    }






















}
