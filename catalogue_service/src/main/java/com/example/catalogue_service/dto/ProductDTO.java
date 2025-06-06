package com.example.catalogue_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDTO {
    private long id;
    @NotNull
    private String title;
    @NotNull
    private double price;
    @NotNull
    private String description;
    private Long previewImageID;



}
