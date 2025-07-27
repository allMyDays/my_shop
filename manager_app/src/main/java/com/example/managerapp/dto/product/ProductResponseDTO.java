package com.example.managerapp.dto.product;

import com.example.managerapp.dto.category.CategoryResponseDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class ProductResponseDTO {

    private Long id;

    private String title;

    private String description;

    private int price;

    private LocalDateTime dateOfCreation;

    private String previewImageFileName;


    private List<String> imageFileNames = new ArrayList<>();







}
