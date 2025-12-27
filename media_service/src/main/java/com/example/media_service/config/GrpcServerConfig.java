package com.example.media_service.config;

import com.example.media_service.controller.grpc.MediaGrpcService;
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

    private final MediaGrpcService mediaGrpcService;
    @Value("${server.grpc.port}")
    private int port;

    @PostConstruct
    public void startServer() throws IOException {
        Server server = ServerBuilder
                .forPort(port)
                .addService(mediaGrpcService)
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

    }



}
