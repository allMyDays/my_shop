package com.example.common.client.grpc;

import com.example.common.dto.CategoryResponseDTO;
import com.example.common.grpc.category.Category;
import com.example.common.grpc.category.CategoryServiceGrpc;
import com.example.common.mapper.grpc.CategoryMapper;
import com.google.protobuf.Empty;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnClass(value = {
        ManagedChannel.class,
        ManagedChannelBuilder.class,
        EurekaClient.class
})
@RequiredArgsConstructor
public class CategoryGrpcClient {

    private final ObjectProvider<CategoryServiceGrpc.CategoryServiceBlockingStub> blockingStubObjectProvider;

    private final CategoryMapper categoryMapper;


    public List<CategoryResponseDTO> getAllCategories() {

        List<Category.CategoryResponse> categoriesResponse = blockingStubObjectProvider.getObject()
                .getAllCategories(Empty.getDefaultInstance())
                .getCategoriesList();

        return categoryMapper.toCategoryResponseDTOList(categoriesResponse);
    }




}
