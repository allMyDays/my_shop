package com.example.managerapp.dto.wish;

import lombok.Data;

@Data
public class WishItemDTO {

    private Long productId;

    String title;

    int price;

    String previewImageFileName;

}
