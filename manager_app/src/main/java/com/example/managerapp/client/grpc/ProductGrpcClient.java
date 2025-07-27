package com.example.managerapp.client.grpc;

import com.example.catalogue.grpc.*;
import com.example.managerapp.dto.product.ProductResponseDTO;
import com.example.managerapp.mapper.ProductMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductGrpcClient {

    private final ProductMapper productMapper;

    private final ProductServiceGrpc.ProductServiceBlockingStub productStub;


    public Optional<ProductResponseDTO> getProductById(Long id){

        ProductRequestById productRequest = ProductRequestById
                .newBuilder()
                .setId(id)
                .build();

        try{

        ProductResponse productResponse = productStub.getProductById(productRequest);

        return Optional.of(productMapper.toProductResponseDTO(productResponse));

        }catch (StatusRuntimeException e){
            if(e.getStatus().getCode()== Status.Code.NOT_FOUND){
                return Optional.empty();
            }
            throw e;
        }

    }

    public List<ProductResponseDTO> getAllProducts(Long categoryId, String filter) {

        ProductRequestByFilterAndCategory.Builder productRequestBuilder = ProductRequestByFilterAndCategory
                .newBuilder()
                .setFilter(filter);

        if(categoryId != null){
            productRequestBuilder.setCategoryId(categoryId);
        }


        ProductResponseList productResponseList = productStub.getAllProducts(productRequestBuilder.build());

        return productMapper.toProductResponseDTOList(productResponseList.getProductsList());

    }


    public List<ProductResponseDTO> getProductsByIds(List<Long> ids){

        ProductRequestByIds productRequest = ProductRequestByIds
                .newBuilder()
                .addAllIds(ids)
                .build();


        ProductResponseList productResponseList = productStub.getProductsByIds(productRequest);

        return productMapper.toProductResponseDTOList(productResponseList.getProductsList());

    }








}
