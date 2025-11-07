package com.example.catalogue_service.controller.grpc;

import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.mapper.LocalProductMapper;
import com.example.catalogue_service.service.ProductService;
import com.example.common.dto.product.ProductIdAndPriceDto;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.grpc.product.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;

    private final LocalProductMapper productMapper;



    @Override
    public void getProductById(ProductRequestById request, StreamObserver<ProductResponse> responseObserver) {

        Optional<Product> productOptional = productService.getProductByID(request.getId());


          if(productOptional.isEmpty()) {
              responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
              return;
          }

          ProductResponse productResponse = productMapper.toProductResponse(productOptional.get());


        responseObserver.onNext(productResponse);
        responseObserver.onCompleted();


    }

    @Override
    public void getProductsByIds(ProductRequestByIds request, StreamObserver<ProductResponse> responseObserver) {

        try(Stream<Product> productStream = productService.getProductsByIDs(request.getIdsList())){

            productStream.forEach(product -> {

                        responseObserver.onNext(productMapper.toProductResponse(product));

                    }
            );

            responseObserver.onCompleted();
        };


    }

    @Override
    public void getAllProducts(ProductRequestByFilterAndCategory request, StreamObserver<ProductResponse> responseObserver) {

        try(Stream<Product> productStream = productService.getAll(request.getCategoryId(), request.getFilter(), request.getOffset())){

            productStream.forEach(product -> {

                        responseObserver.onNext(productMapper.toProductResponse(product));

                        }
                    );

            responseObserver.onCompleted();
               };


    }
    @Override
    public void productExists(ProductRequestById request, StreamObserver<ProductBooleanResponse> responseObserver) {

        boolean exists = productService.productExists(request.getId());

        responseObserver.onNext(ProductBooleanResponse.newBuilder()
                .setExists(exists)
                .build());
        responseObserver.onCompleted();

    }

    @Override
    public void productsExist(ProductRequestBySingleIds request, StreamObserver<ProductResponseBySingleIds> responseObserver) {

       List<Long> existingIds = productService.productsExist(request.getIdsList());

        responseObserver.onNext(ProductResponseBySingleIds.newBuilder()
                .addAllIds(existingIds)
                .build());
        responseObserver.onCompleted();

    }

    @Override
    public void getTotalPrice(ProductIdAndQuantityDtoOuterResp request, StreamObserver<TotalPriceResponse> responseObserver) {

        List<ProductIdAndQuantityDto> productDTOs = request.getProductInnerRespList().stream()
                .map(a->new ProductIdAndQuantityDto(a.getProductId(), a.getProductQuantity()))
                .toList();

        responseObserver.onNext(TotalPriceResponse.newBuilder()
                .setPrice(productService.getTotalPrice(productDTOs))
                .build());
        responseObserver.onCompleted();

    }

    public void getProductsPrice(ProductRequestBySingleIds request,StreamObserver<ProductIdAndPriceOuterResp> responseObserver) {

        List<ProductIdAndPriceDto> priceDTOs = productService.getProductsPrice(request.getIdsList());

        List<ProductIdAndPriceInnerResp> productDto =priceDTOs.stream()
                .map(a-> ProductIdAndPriceInnerResp.newBuilder()
                                .setProductId(a.getProductId())
                                .setProductPrice(a.getProductPrice())
                                .build()
                ).toList();

        responseObserver.onNext(ProductIdAndPriceOuterResp.newBuilder()
                .addAllProductInnerResp(productDto)
                .build());

        responseObserver.onCompleted();

    }



}
