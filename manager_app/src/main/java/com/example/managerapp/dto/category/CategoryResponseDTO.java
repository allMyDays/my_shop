package com.example.managerapp.dto.category;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CategoryResponseDTO {

    private Long id;

    private String name;

}
