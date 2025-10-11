package com.example.media_service.controller.rest;

import com.example.media_service.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaRestController {

    private final MinioService minioService;

    @GetMapping("/get/{fileName}")
    public ResponseEntity<byte[]> getMediaFile(@PathVariable String fileName) throws IOException {

        try{
            Map.Entry<byte[], MediaType> image = minioService.getFile(fileName);

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
