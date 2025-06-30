package com.example.managerapp.rest;


import com.example.managerapp.entity.Bucket;
import com.example.managerapp.entity.MyUser;
import com.example.managerapp.entity.Product;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BucketRestClient {
    private final UserRestClient userRestClient;



    public Bucket createBucket(MyUser user, List<Long> productIDs) {
     /*   Bucket bucket = new Bucket();
        bucket.setUser(user);
        List<Product> products = getProductsByIDs(productIDs);
        bucket.setProducts(products);
        return bucketRepository.save(bucket);*/
        return null;
    }

    public void addProductsToBucket(Bucket bucket, List<Long> productIDs) {
       /* List<Product> products = bucket.getProducts()==null?new ArrayList<>():bucket.getProducts();
        products.addAll(getProductsByIDs(productIDs));
        bucket.setProducts(products);
        bucketRepository.save(bucket);*/

    }

    public Bucket getBucketDtoByUser(String email) {
      /*  MyUser user = userService.findByEmail(email);
        BucketDTO bucketDTO = new BucketDTO();
        if(user==null||user.getBucket()==null ) return bucketDTO;

        Map<Long, BucketDetailsDTO> tempMap = new HashMap<>();

        List<Product> products = user.getBucket().getProducts();
        for(Product product : products){
         BucketDetailsDTO detail = tempMap.get(product.getId());
         if(detail==null){
                tempMap.put(product.getId(), new BucketDetailsDTO(product));

         }else {
             detail.setProductQuantity(detail.getProductQuantity()+1);
             detail.setSum(detail.getSum()+product.getPrice());
         }
        }
        bucketDTO.setBucketDetailsList(new ArrayList<>(tempMap.values()));
        bucketDTO.aggregate();

        return bucketDTO;*/
        return null;

    }


    private List<Product> getProductsByIDs(List<Long> productIDs){
       /* return productIDs.stream()
                .map(productRepository::getOne) //todo
                .collect(Collectors.toList());*/
        return null;


    }
}
