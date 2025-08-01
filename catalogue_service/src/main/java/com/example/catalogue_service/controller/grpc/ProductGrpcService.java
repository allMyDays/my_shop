package com.example.catalogue_service.controller.grpc;

import com.example.catalogue.grpc.*;
import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.mapper.ProductMapper;
import com.example.catalogue_service.repository.ProductRepository;
import com.example.catalogue_service.service.ProductService;
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

    private final ProductMapper productMapper;



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
    public void getProductsByIds(ProductRequestByIds request,StreamObserver<ProductResponse> responseObserver) {

        try(Stream<Product> productStream = productService.getProductsByIDs(request.getIdsList(), request.getLimit(), request.getOffset())){

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



}
