package com.example.common.dto.category.rest;


import com.example.common.enumeration.category.CategoryCode;
import lombok.*;

@AllArgsConstructor
@Getter
public class CategoryResponseDTO {

    private Long id;

    private String name;

    private CategoryCode code;

}
