package com.example.catalogue_service.service.i;


import com.example.catalogue_service.dto.BucketDTO;
import com.example.catalogue_service.entity.Bucket;
import com.example.catalogue_service.entity.MyUser;

import java.util.List;

public interface BucketService {
    Bucket createBucket(MyUser user, List<Long> productIDs);

    void addProductsToBucket(Bucket bucket, List<Long> productIDs);

    BucketDTO getBucketDtoByUser(String email);
}
