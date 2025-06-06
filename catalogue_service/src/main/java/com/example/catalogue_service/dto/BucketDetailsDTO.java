package com.example.catalogue_service.dto;

import com.example.catalogue_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketDetailsDTO {
    private String title;
    private long productID;
    private int price;
    private int productQuantity;
    private int sum;

    public BucketDetailsDTO(Product product) {
        this.title = product.getTitle();
        this.productID = product.getId();
        this.price = product.getPrice();
        this.productQuantity=1;
        this.sum=product.getPrice();




    }




}
