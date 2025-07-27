package com.example.managerapp.config;

import com.example.catalogue.grpc.CategoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.ManagedChannelImplBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.catalogue.grpc.ProductServiceGrpc;

@Configuration
public class GrpcClientBeans {

    @Bean
    public ManagedChannel catalogueChannel() {

        return ManagedChannelBuilder   // создает канал связи с grpc сервером
                .forAddress("localhost",82)
                .usePlaintext()
                .build();
    }

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceStub(@Qualifier("catalogueChannel") ManagedChannel channel) {

        return ProductServiceGrpc.newBlockingStub(channel);   // создает grpc-клиент, через который можно вызывать удаленные методы

    }

    @Bean
    public CategoryServiceGrpc.CategoryServiceBlockingStub categoryServiceStub(@Qualifier("catalogueChannel") ManagedChannel channel) {

        return CategoryServiceGrpc.newBlockingStub(channel);
    }

}
