package com.example.media_service.controller.rest.image;

import com.example.common.exception.UserNotFoundException;
import com.example.media_service.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.util.Map;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media/images/user/avatar")
public class UserAvatarController {

    private final ImageService imageService;


    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> saveUserAvatar(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) throws IOException, UserNotFoundException {

        imageService.saveUserAvatar(file, getMyUserEntityId(jwt));

        return ResponseEntity.ok()
                .build();

    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUserAvatar(@AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

       imageService.deleteUserAvatar(true,getMyUserEntityId(jwt));

        return ResponseEntity.ok()
                .build();

    }

    @GetMapping("/get/{fileName}")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable String fileName) throws IOException {

        try{
            Map.Entry<byte[], MediaType> image = imageService.getUserAvatar(fileName);

            return ResponseEntity
                    .ok()
                    .contentType(image.getValue())
                    .body(image.getKey());

        }catch (NoSuchKeyException e){
            return ResponseEntity
                    .notFound()
                    .build();
        }

    }







}
