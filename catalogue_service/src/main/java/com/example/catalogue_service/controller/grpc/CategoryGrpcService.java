package com.example.catalogue_service.controller.grpc;


import com.example.catalogue.grpc.CategoryServiceGrpc;
import com.example.catalogue_service.entity.Category;
import com.example.catalogue_service.mapper.CategoryMapper;
import com.example.catalogue_service.service.CategoryService;
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

    private final CategoryMapper categoryMapper;


    @Override
    public void getAllCategories(Empty request, StreamObserver<com.example.catalogue.grpc.Category.CategoryResponseList> responseObserver){

        List<Category> categories = categoryService.findAllCategories();

        responseObserver.onNext(

                com.example.catalogue.grpc.Category.CategoryResponseList
                        .newBuilder()
                        .addAllCategories(categoryMapper.toCategoryResponseList(categories))
                        .build());
        responseObserver.onCompleted();

    }








}
