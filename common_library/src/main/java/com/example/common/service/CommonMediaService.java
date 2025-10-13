package com.example.common.service;

import com.example.common.exception.FileIsNotImageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CommonMediaService {

    public static void validateImages(List<MultipartFile> images) {
        if (images == null) return;

        for (MultipartFile image : images) {
            String contentType = image.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new FileIsNotImageException(contentType);
            }
        }
    }






}
