package com.example.common.dto.wish.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WishItemResponseDTO {

    private Long productId;

    String title;

    int price;

    String previewImageFileName;


}
