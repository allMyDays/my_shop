package com.example.catalogue_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDTO {
    private long id;
    @NotNull(message = "title must not be null")
    private String title;
    @NotNull
    private int price;
    @NotNull(message = "description must not be null")
    private String description;
    private Long previewImageID;



}
