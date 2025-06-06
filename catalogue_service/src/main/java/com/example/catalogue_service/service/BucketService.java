package com.example.catalogue_service.service;


import com.example.catalogue_service.dto.BucketDTO;
import com.example.catalogue_service.dto.BucketDetailsDTO;
import com.example.catalogue_service.entity.Bucket;
import com.example.catalogue_service.entity.MyUser;
import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.repository.BucketRepository;
import com.example.catalogue_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BucketService implements com.example.catalogue_service.service.i.BucketService {
    private final BucketRepository bucketRepository;
    private final ProductRepository productRepository;
    private final UserService userService;



    @Override
    public Bucket createBucket(MyUser user, List<Long> productIDs) {
        Bucket bucket = new Bucket();
        bucket.setUser(user);
        List<Product> products = getProductsByIDs(productIDs);
        bucket.setProducts(products);
        return bucketRepository.save(bucket);
    }

    public void addProductsToBucket(Bucket bucket, List<Long> productIDs) {
        List<Product> products = bucket.getProducts()==null?new ArrayList<>():bucket.getProducts();
        products.addAll(getProductsByIDs(productIDs));
        bucket.setProducts(products);
        bucketRepository.save(bucket);

    }

    @Override
    public BucketDTO getBucketDtoByUser(String email) {
        MyUser user = userService.findByEmail(email);
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

        return bucketDTO;

    }


    private List<Product> getProductsByIDs(List<Long> productIDs){
        return productIDs.stream()
                .map(productRepository::getOne) //todo
                .collect(Collectors.toList());


    }
}
