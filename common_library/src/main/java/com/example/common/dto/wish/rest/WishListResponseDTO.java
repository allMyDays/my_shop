package com.example.common.dto.wish.rest;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WishListResponseDTO {

    private long totalQuantity;

    private List<WishItemResponseDTO> itemsDTOList = new ArrayList<>();

}
