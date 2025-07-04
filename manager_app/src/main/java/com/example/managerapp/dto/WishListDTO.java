package com.example.managerapp.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WishListDTO {

    private int totalQuantity;

    private List<WishItemDTO> itemsDTOList = new ArrayList<>();







}
