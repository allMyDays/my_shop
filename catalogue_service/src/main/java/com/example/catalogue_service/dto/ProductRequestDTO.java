package com.example.catalogue_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductRequestDTO {
    @NotNull
    @Size(min = 5, max = 50)
    private String title;

    @NotNull
    @Size(min = 5, max = 5000000)
    private int price;

    @NotNull(message = "description must not be null")
    @Size(min = 10, max = 1000)
    private String description;







}
