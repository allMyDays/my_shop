package com.example.catalogue_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketDTO {
    private int uniqueProductQuantity;
    private int productQuantity;
    private int sum;
    private List<BucketDetailsDTO> bucketDetailsList = new ArrayList<>();

    public void aggregate(){
       this.uniqueProductQuantity = bucketDetailsList.size();
       this.productQuantity = bucketDetailsList.stream()
               .mapToInt(BucketDetailsDTO::getProductQuantity).sum();
       this.sum = bucketDetailsList.stream().mapToInt(BucketDetailsDTO::getSum).sum();


    }




}
