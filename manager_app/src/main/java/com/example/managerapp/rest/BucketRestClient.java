package com.example.managerapp.rest;


import com.example.managerapp.entity.Cart;
import com.example.managerapp.entity.MyUser;
import com.example.managerapp.entity.ProductRecord;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BucketRestClient {
    private final UserRestClient userRestClient;



    public Cart createBucket(MyUser user, List<Long> productIDs) {
     /*   Bucket bucket = new Bucket();
        bucket.setUser(user);
        List<ProductRecord> products = getProductsByIDs(productIDs);
        bucket.setProducts(products);
        return bucketRepository.save(bucket);*/
        return null;
    }

    public void addProductsToBucket(Cart bucket, List<Long> productIDs) {
       /* List<ProductRecord> products = bucket.getProducts()==null?new ArrayList<>():bucket.getProducts();
        products.addAll(getProductsByIDs(productIDs));
        bucket.setProducts(products);
        bucketRepository.save(bucket);*/

    }

    public Cart getBucketDtoByUser(String email) {
      /*  MyUser user = userService.findByEmail(email);
        BucketDTO bucketDTO = new BucketDTO();
        if(user==null||user.getBucket()==null ) return bucketDTO;

        Map<Long, BucketDetailsDTO> tempMap = new HashMap<>();

        List<ProductRecord> products = user.getBucket().getProducts();
        for(ProductRecord productRecord : products){
         BucketDetailsDTO detail = tempMap.get(productRecord.getId());
         if(detail==null){
                tempMap.put(productRecord.getId(), new BucketDetailsDTO(productRecord));

         }else {
             detail.setProductQuantity(detail.getProductQuantity()+1);
             detail.setSum(detail.getSum()+productRecord.getPrice());
         }
        }
        bucketDTO.setBucketDetailsList(new ArrayList<>(tempMap.values()));
        bucketDTO.aggregate();

        return bucketDTO;*/
        return null;

    }


    private List<ProductRecord> getProductsByIDs(List<Long> productIDs){
       /* return productIDs.stream()
                .map(productRepository::getOne) //todo
                .collect(Collectors.toList());*/
        return null;


    }
}
