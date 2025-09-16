package com.example.media_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SaveProductImageDto {
    @NotBlank
    private Long productId;
    @NotBlank
    private boolean previewImage;


}
