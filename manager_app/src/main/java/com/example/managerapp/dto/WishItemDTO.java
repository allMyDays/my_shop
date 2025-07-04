package com.example.managerapp.dto;

import lombok.Data;

@Data
public class WishItemDTO {

    private Long productId;

    String title;

    int price;

    Long previewImageID;

}
