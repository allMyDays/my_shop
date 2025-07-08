package com.example.catalogue_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SendProductDTO {

    private long id;

    private String title;

    private String description;

    private int price;

    private String previewImageFileName;

    private LocalDateTime dateOfCreation;






}
