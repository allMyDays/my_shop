package com.example.managerapp.controller.rest;


import com.example.managerapp.entity.enums.MinIO_bucket;
import com.example.managerapp.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/save")
    public Map<String, Long> saveImage(@RequestParam("file") MultipartFile file) throws IOException {
        Long fileID =  minioService.saveFile(file,MinIO_bucket.products);
        return Map.of("ID",fileID);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) throws IOException {

        byte[] imagesByte = minioService.getFile(fileName,MinIO_bucket.products);

        String contentType = Optional.ofNullable(
                URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imagesByte)))
                .orElse("application/octet-stream");


        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imagesByte);

    }












}
