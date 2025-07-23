package com.example.managerapp.controller.rest;


import com.example.managerapp.entity.MyUser;
import com.example.managerapp.entity.enums.MinIO_bucket;
import com.example.managerapp.service.ImageService;
import com.example.managerapp.service.MinioService;
import com.example.managerapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final MinioService minioService;

    private final ImageService imageService;

    @PostMapping("/save_product_image")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> saveProductImage(@RequestParam MultipartFile file, @RequestParam Long productId, @RequestParam boolean isPreview, OAuth2AuthenticationToken authenticationToken) throws IOException {
       // Long fileID =  minioService.saveFile(file,MinIO_bucket.products);
       // return Map.of("ID",fileID);
        return ResponseEntity.ok()
                .build();
    }

    @PostMapping("/save_user_avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> saveUserAvatar(@RequestParam("file") MultipartFile file, OAuth2AuthenticationToken authenticationToken) throws IOException {

        imageService.saveUserAvatar(file, authenticationToken);

        return ResponseEntity.ok()
                .build();

    }

    @DeleteMapping("delete_user_avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUserAvatar(OAuth2AuthenticationToken authenticationToken) throws IOException {

        imageService.deleteUserAvatar(authenticationToken);

        return ResponseEntity.ok()
                .build();

    }




    @GetMapping("/get_product_image/{fileName}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String fileName) throws IOException {

        byte[] imagesByte = minioService.getFile(fileName,MinIO_bucket.products);

        String contentType = Optional.ofNullable(
                URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imagesByte)))
                .orElse("application/octet-stream");


        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imagesByte);

    }

    @GetMapping("/get_user_avatar/{fileName}")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable String fileName) throws IOException {

        byte[] imagesByte = minioService.getFile(fileName,MinIO_bucket.users);

        String contentType = Optional.ofNullable(
                        URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imagesByte)))
                .orElse("application/octet-stream");


        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imagesByte);

    }












}
