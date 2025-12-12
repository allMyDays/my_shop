package com.example.common.dto.product.rest;


import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductResponseDTO {
    private Long id;

    private String title;

    private String description;

    private String priceView;

    private int priceInt;

    private String previewImageFileName;

    private List<String> imageFileNames = new ArrayList<>();

    private String article;

}
