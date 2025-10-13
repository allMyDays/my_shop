package com.example.common.client.grpc;

import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.grpc.product.*;
import com.example.common.mapper.grpc.ProductMapper;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@ConditionalOnClass(value = {
        ManagedChannel.class,
        ManagedChannelBuilder.class,
        EurekaClient.class
})
@RequiredArgsConstructor
public class ProductGrpcClient {

    private final ProductMapper productMapper;

    private final ObjectProvider<ProductServiceGrpc.ProductServiceBlockingStub> productBlockingStubObjectProvider;

    private final ObjectProvider<ProductServiceGrpc.ProductServiceStub> productAsyncStubObjectProvider;



    public Optional<ProductResponseDTO> getProductById(@NonNull @Positive Long id){

        ProductRequestById productRequest = ProductRequestById
                .newBuilder()
                .setId(id)
                .build();

        try{

        ProductResponse productResponse = productBlockingStubObjectProvider.getObject()
                .getProductById(productRequest);

        return Optional.of(productMapper.toProductResponseDTO(productResponse));

        }catch (StatusRuntimeException e){
            if(e.getStatus().getCode()== Status.Code.NOT_FOUND){
                return Optional.empty();
            }
            throw e;
        }

    }
    public void lazyLoadProductBatchStream(Optional<Long> categoryId,
                                           @NonNull String filter,
                                           @NonNull int offset,
                                           @NonNull Consumer<ProductResponseDTO> consumer,
                                           @NonNull Runnable onComplete){

        ProductRequestByFilterAndCategory.Builder productRequestBuilder = ProductRequestByFilterAndCategory
                .newBuilder()
                .setOffset(offset)
                .setFilter(filter);

        categoryId.ifPresent(productRequestBuilder::setCategoryId);


        productAsyncStubObjectProvider.getObject()
                .getAllProducts(productRequestBuilder.build(), new StreamObserver<ProductResponse>() {


            @Override
            public void onNext(ProductResponse productResponse) {

                consumer.accept(productMapper.toProductResponseDTO(productResponse));  // отдать товар наружу

            }

            @Override
            public void onError(Throwable throwable) {
                onComplete.run();
            }

            @Override
            public void onCompleted() {   // когда все товары получены
                onComplete.run();   //  чтобы вызвать emitter.complete();

            }
        });

    }

    public List<ProductResponseDTO> getProductsByIdsFullList(@NonNull List<Long> ids){

        ProductRequestByIds productRequest = ProductRequestByIds
                .newBuilder()
                .addAllIds(ids)
                .setLimit(100_000)
                .setOffset(0)
                .build();


        Iterator<ProductResponse> productResponseIterator = productBlockingStubObjectProvider.getObject()
                .getProductsByIds(productRequest);

        List<ProductResponseDTO> productResponseDTOS = new ArrayList<>();

        while(productResponseIterator.hasNext()){
            productResponseDTOS.add(productMapper.toProductResponseDTO(productResponseIterator.next()));
        }

        return productResponseDTOS;

    }

    public void getProductsByIdsStream(@NonNull List<Long> ids,
                                       @NonNull int limit,
                                       @NonNull int offset,
                                       @NonNull Consumer<ProductResponseDTO> consumer,
                                       @NonNull Runnable onComplete){

        ProductRequestByIds productRequest = ProductRequestByIds
                .newBuilder()
                .addAllIds(ids)
                .setLimit(limit)
                .setOffset(offset)
                .build();

        productAsyncStubObjectProvider.getObject()
                .getProductsByIds(productRequest, new StreamObserver<ProductResponse>() {


            @Override
            public void onNext(ProductResponse productResponse) {

                consumer.accept(productMapper.toProductResponseDTO(productResponse));  // отдать товар наружу

            }

            @Override
            public void onError(Throwable throwable) {
                onComplete.run();
            }

            @Override
            public void onCompleted() {
                onComplete.run();

            }
        });

    }

    public boolean productExists(@NonNull Long id){

        ProductRequestById productRequest = ProductRequestById
                .newBuilder()
                .setId(id)
                .build();
        try{

            ProductBooleanResponse productBooleanResponse = productBlockingStubObjectProvider.getObject()
                    .productExists(productRequest);
            return productBooleanResponse.getExists();

        }catch (StatusRuntimeException e){
                return false;
            }
        }


}









