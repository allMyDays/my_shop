package com.example.common.client.grpc;

import com.example.common.dto.product.ProductIdAndPriceDto;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.grpc.product.*;
import com.example.common.mapper.ProductMapper;
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



    public Optional<ProductResponseDTO> getProductById(@Positive long id){

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
                                           int offset,
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
                                       @NonNull Consumer<ProductResponseDTO> consumer,
                                       @NonNull Runnable onComplete){

        ProductRequestByIds productRequest = ProductRequestByIds
                .newBuilder()
                .addAllIds(ids)
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

    public boolean productExists(long id){

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

    public List<Long> productsExist(@NonNull List<Long> productIds){

       try{ ProductRequestBySingleIds productRequest = ProductRequestBySingleIds
                .newBuilder()
                .addAllIds(productIds)
                .build();

            ProductResponseBySingleIds productResponse = productBlockingStubObjectProvider.getObject()
                    .productsExist(productRequest);
            return productResponse.getIdsList();

       }catch (StatusRuntimeException e){
           e.printStackTrace();
           throw e;
       }

    }

    public int getTotalPrice(@NonNull List<ProductIdAndQuantityDto> productIdAndQuantityDTOs){
        if(productIdAndQuantityDTOs.isEmpty()){
            return 0;
        }
        try{

            ProductIdAndQuantityDtoOuterResp productOuterResp = ProductIdAndQuantityDtoOuterResp
                    .newBuilder()
                    .addAllProductInnerResp(
                            productIdAndQuantityDTOs.stream()
                                    .map(a->
                                            ProductIdAndQuantityDtoInnerResp.newBuilder()
                                                    .setProductId(a.getProductId())
                                                    .setProductQuantity(a.getProductQuantity())
                                                    .build()
                                    ).toList()


                    ).build();

            TotalPriceResponse priceResponse = productBlockingStubObjectProvider.getObject()
                    .getTotalPrice(productOuterResp);

            return priceResponse.getPrice();

        }catch (StatusRuntimeException e){
            e.printStackTrace();
            throw e;
        }

    }

    public List<ProductIdAndPriceDto> getProductsPrice(@NonNull List<Long> productIds){

        ProductRequestBySingleIds productRequest = ProductRequestBySingleIds
                .newBuilder()
                .addAllIds(productIds)
                .build();

        ProductIdAndPriceOuterResp response =  productBlockingStubObjectProvider.getObject().getProductsPrice(productRequest);

        return response.getProductInnerRespList().stream()
                .map(a->new ProductIdAndPriceDto(a.getProductId(), a.getProductPrice()))
                .toList();

    }


}









