package com.example.media_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DeleteProductImageDto {
    @NotNull
    private Long productId;
    @NotNull
    private String fileName;
    @NotNull
    private boolean previewImage;


}
