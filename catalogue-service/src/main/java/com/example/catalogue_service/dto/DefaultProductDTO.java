package com.example.catalogue_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DefaultProductDTO {
    private String code;
    private String title;
    private int price;
    private String description;
    private String avatar;
    private String category;
    private List<String> images;
}
