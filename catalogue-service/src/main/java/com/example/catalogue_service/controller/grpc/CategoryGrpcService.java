package com.example.catalogue_service.controller.grpc;


import com.example.catalogue_service.entity.Category;
import com.example.catalogue_service.mapper.LocalCategoryMapper;
import com.example.catalogue_service.service.CategoryService;
import com.example.common.grpc.category.CategoryServiceGrpc;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryGrpcService extends CategoryServiceGrpc.CategoryServiceImplBase {

    private final CategoryService categoryService;

    private final LocalCategoryMapper localCategoryMapper;


    @Override
    public void getAllCategories(Empty request, StreamObserver<com.example.common.grpc.category.Category.CategoryResponseList> responseObserver){

        List<Category> categories = categoryService.findAllCategories();

        responseObserver.onNext(

                com.example.common.grpc.category.Category.CategoryResponseList
                        .newBuilder()
                        .addAllCategories(localCategoryMapper.toCategoryResponseList(categories))
                        .build());
        responseObserver.onCompleted();

    }








}
