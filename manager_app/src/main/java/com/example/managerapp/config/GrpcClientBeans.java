package com.example.managerapp.config;

import com.example.catalogue.grpc.CategoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.catalogue.grpc.ProductServiceGrpc;

@Configuration
public class GrpcClientBeans {

    @Bean
    public ManagedChannel catalogueChannel(@Value("${my_shop.services.catalogue.grpc.name}") String name,
                                           @Value("${my_shop.services.catalogue.grpc.port}") int port) {

        return ManagedChannelBuilder   // создает канал связи с grpc сервером
                .forAddress(name,port)
                .usePlaintext()
                .build();
    }

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productBlockingStub(@Qualifier("catalogueChannel") ManagedChannel channel) {

        return ProductServiceGrpc.newBlockingStub(channel);   // создает grpc-клиент, через который можно вызывать удаленные методы

    }

    @Bean
    public ProductServiceGrpc.ProductServiceStub productAsyncStub(@Qualifier("catalogueChannel") ManagedChannel channel) {

        return ProductServiceGrpc.newStub(channel);

    }


    @Bean
    public CategoryServiceGrpc.CategoryServiceBlockingStub categoryBlockingStub(@Qualifier("catalogueChannel") ManagedChannel channel) {

        return CategoryServiceGrpc.newBlockingStub(channel);
    }

}
