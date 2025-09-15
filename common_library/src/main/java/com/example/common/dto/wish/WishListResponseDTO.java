package com.example.common.dto.wish;


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

    private int totalQuantity;

    private List<WishItemResponseDTO> itemsDTOList = new ArrayList<>();

}
