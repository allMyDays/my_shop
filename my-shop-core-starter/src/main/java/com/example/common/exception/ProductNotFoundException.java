package com.example.common.exception;

import lombok.NonNull;
import org.apache.kafka.common.protocol.types.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ProductNotFoundException extends RuntimeException{

    List<Long> productIds;

    public ProductNotFoundException(@NonNull List<Long> productIds){

        super("no products with the following ids were found: "
                +productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")));

        this.productIds=productIds;

    }

    public List<Long> getProductIds() {
        return productIds;
    }
}
