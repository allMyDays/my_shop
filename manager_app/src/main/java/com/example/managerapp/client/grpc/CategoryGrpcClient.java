package com.example.managerapp.client.grpc;

import com.example.catalogue.grpc.Category;
import com.example.catalogue.grpc.CategoryServiceGrpc;
import com.example.managerapp.dto.category.CategoryResponseDTO;
import com.example.managerapp.mapper.CategoryMapper;
import com.google.protobuf.Empty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryGrpcClient {

    private final CategoryServiceGrpc.CategoryServiceBlockingStub blockingStub;

    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDTO> getAllCategories() {

        List<Category.CategoryResponse> categoriesResponse = blockingStub.getAllCategories(Empty.getDefaultInstance()).getCategoriesList();

        return categoryMapper.toCategoryResponseDTOList(categoriesResponse);
    }




}
