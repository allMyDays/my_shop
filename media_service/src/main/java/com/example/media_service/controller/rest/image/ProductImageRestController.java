package com.example.media_service.controller.rest.image;

import com.example.common.exception.UserNotFoundException;
import com.example.media_service.dto.DeleteProductImageDto;
import com.example.media_service.dto.SaveProductImageDto;
import com.example.media_service.entity.enumeration.MinIO_bucket;
import com.example.media_service.service.MinioService;
import com.example.media_service.service.image.ImageService;
import jakarta.validation.constraints.NotNull;
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

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media/images/product")
public class ProductImageRestController {

    private final ImageService imageService;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> saveProductImage(@RequestPart("file") MultipartFile file, @RequestPart("data") SaveProductImageDto saveImageDto, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        imageService.saveProductImage(saveImageDto.getProductId(),file,saveImageDto.isPreviewImage(),getMyUserEntityId(jwt));
        return ResponseEntity.ok()
                .build();
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductImage(@RequestBody DeleteProductImageDto deleteImageDto, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        imageService.deleteProductImage(deleteImageDto.getProductId(), deleteImageDto.getFileName(), deleteImageDto.isPreviewImage(), getMyUserEntityId(jwt));

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
