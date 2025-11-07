package com.example.common.config.grpc;
import com.example.common.grpc.delivery.DeliveryServiceGrpc;
import com.example.common.grpc.user.UserServiceGrpc;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.print.attribute.standard.OrientationRequested;

@Configuration
@ConditionalOnClass(value = {
                    ManagedChannel.class,
                    ManagedChannelBuilder.class
            })
public class GrpcOrderClientBeans {

        @Bean
        @Lazy
        public ManagedChannel orderChannel(EurekaClient eurekaClient) {

            InstanceInfo instanceInfo = eurekaClient.getApplication("order-service")
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
        public DeliveryServiceGrpc.DeliveryServiceBlockingStub orderBlockingStub(@Qualifier("orderChannel") ManagedChannel channel) {

            return DeliveryServiceGrpc.newBlockingStub(channel);

        }

    }






