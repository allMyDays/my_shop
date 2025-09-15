package com.example.media_service.controller.rest.image;

import com.example.common.exception.UserNotFoundException;
import com.example.media_service.entity.enumeration.MinIO_bucket;
import com.example.media_service.service.MinioService;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media/images/product")
public class ProductImageRestController {

    private final ImageService imageService;

    @PostMapping("/save")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> saveProductImage(@RequestParam Long productId, @RequestParam MultipartFile file, @RequestParam boolean preview, @AuthenticationPrincipal Jwt jwt) throws IOException, UserNotFoundException {

        imageService.saveProductImage(productId,file,preview,jwt);
        return ResponseEntity.ok()
                .build();
    }
    @GetMapping("/get/{fileName}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String fileName) throws IOException {

       try{
           Map.Entry<byte[],MediaType> image = imageService.getProductImage(fileName);

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
