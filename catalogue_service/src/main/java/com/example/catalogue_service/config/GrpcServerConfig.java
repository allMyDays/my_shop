package com.example.catalogue_service.config;

import com.example.catalogue_service.controller.grpc.CategoryGrpcService;
import com.example.catalogue_service.controller.grpc.ProductGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {

    private final ProductGrpcService productGrpcService;
    private final CategoryGrpcService categoryGrpcService;
    @Value("${server.grpc.port}")
    private int port;

    @PostConstruct
    public void startServer() throws IOException {
        Server server = ServerBuilder
                .forPort(port)
                .addService(productGrpcService)
                .addService(categoryGrpcService)
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

    }



}
