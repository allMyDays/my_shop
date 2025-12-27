package com.example.common.config.grpc;
import com.example.common.grpc.delivery.DeliveryServiceGrpc;
import com.example.common.grpc.media.MediaServiceGrpc;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnClass(value = {
                    ManagedChannel.class,
                    ManagedChannelBuilder.class
            })
public class GrpcMediaClientBeans {

        @Bean
        @Lazy
        public ManagedChannel mediaChannel(EurekaClient eurekaClient) {

            InstanceInfo instanceInfo = eurekaClient.getApplication("media-service")
                    .getInstances()
                    .get(0);
            int port = Integer.parseInt(instanceInfo.getMetadata().get("grpc-port"));

            return ManagedChannelBuilder
                    .forAddress(instanceInfo.getIPAddr(), port)
                    .usePlaintext()
                    .build();
        }

        @Bean
        @Lazy
        public MediaServiceGrpc.MediaServiceBlockingStub mediaBlockingStub(@Qualifier("mediaChannel") ManagedChannel channel) {

            return MediaServiceGrpc.newBlockingStub(channel);

        }

    }






