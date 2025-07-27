package com.example.catalogue_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductResponseDTO {

    private long id;

    private String title;

    private String description;

    private int price;

    private String previewImageFileName;

    private LocalDateTime dateOfCreation;

    private List<String> imageFileNames = new ArrayList<>();





}
