package com.example.common.dto.product.rest;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductMinimalInfoResponseDto {

    private Long id;

    private String title;

    private int price;

    private String previewImageFileName;




}
