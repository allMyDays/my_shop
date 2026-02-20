package com.example.user_service.config;

import com.example.user_service.controller.grpc.UserGrpcService;
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

    private final UserGrpcService userGrpcService;

    @Value("${server.grpc.port}")
    private int port;

    @PostConstruct
    public void startServer() throws IOException {
        Server server = ServerBuilder
                .forPort(port)
                .addService(userGrpcService)
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

    }



}
