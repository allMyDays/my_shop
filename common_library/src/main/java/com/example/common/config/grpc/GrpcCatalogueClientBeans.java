package com.example.common.config.grpc;

import com.example.common.grpc.category.CategoryServiceGrpc;
import com.example.common.grpc.product.ProductServiceGrpc;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.CloseableThreadContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnClass(value = {
        ManagedChannel.class,
        ManagedChannelBuilder.class
})
public class GrpcCatalogueClientBeans {

    @Bean
    @Lazy
    public ManagedChannel catalogueChannel(EurekaClient eurekaClient) {

        InstanceInfo instanceInfo = eurekaClient.getApplication("catalogue-service")
                .getInstances()
                .get(0);
        int port = Integer.parseInt(instanceInfo.getMetadata().get("grpc-port"));

        return ManagedChannelBuilder   // создает канал связи с grpc сервером
                .forAddress(instanceInfo.getIPAddr(),port)
                .usePlaintext()
                .build();
    }

    @Bean
    @Lazy
    public ProductServiceGrpc.ProductServiceBlockingStub productBlockingStub(@Qualifier("catalogueChannel") ManagedChannel channel) {

        return ProductServiceGrpc.newBlockingStub(channel);

    }

    @Bean
    @Lazy
    public ProductServiceGrpc.ProductServiceStub productAsyncStub(@Qualifier("catalogueChannel") ManagedChannel channel) {

        return ProductServiceGrpc.newStub(channel);

    }


    @Bean
    @Lazy
    public CategoryServiceGrpc.CategoryServiceBlockingStub categoryBlockingStub(@Qualifier("catalogueChannel") ManagedChannel channel) {

        return CategoryServiceGrpc.newBlockingStub(channel);
    }


}
