package com.example.catalogue_service.controller.rest;

import com.example.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/catalogue/products/image")
public class ProductImageRestController {


  /*  @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> saveProductImage(@RequestPart("file") MultipartFile file, @RequestPart("data") SaveProductImageDto saveImageDto, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        imageService.saveProductImage(saveImageDto.getProductId(),file,saveImageDto.isPreviewImage());
        return ResponseEntity.ok()
                .build();
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductImage(@RequestBody DeleteProductImageDto deleteImageDto, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {

        imageService.deleteProductImage(deleteImageDto.getProductId(), deleteImageDto.getFileName(), deleteImageDto.isPreviewImage(), getMyUserEntityId(jwt));

        return ResponseEntity.ok()
                .build();

    }*/




}
