package com.example.common.client.grpc;

import com.example.common.dto.user.rest.UserAddressDto;
import com.example.common.grpc.delivery.Delivery;
import com.example.common.grpc.delivery.DeliveryServiceGrpc;
import com.example.common.grpc.user.User;
import com.example.common.grpc.user.UserServiceGrpc;
import com.example.common.mapper.AddressMapper;
import com.netflix.appinfo.EurekaAccept;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnClass(value = {
        ManagedChannel.class,
        ManagedChannelBuilder.class,
        EurekaAccept.class

})
@RequiredArgsConstructor
public class OrderGrpcClient {

    private final AddressMapper addressMapper;

    private final ObjectProvider<DeliveryServiceGrpc.DeliveryServiceBlockingStub> deliveryBlockingStubProvider;


    public Optional<UserAddressDto> getUserAddress(@Positive long userEntityId){

        Delivery.IdRequest oneUserRequest = Delivery.IdRequest
                .newBuilder()
                .setUserEntityId(userEntityId)
                .build();
        try{

             Delivery.AddressResponse addressResponse = deliveryBlockingStubProvider.getObject()
                     .getUserAddress(oneUserRequest);

            return Optional.of(addressMapper.toAddressDto(addressResponse));

        }catch (StatusRuntimeException e){
            if(e.getStatus().getCode()== Status.Code.NOT_FOUND){
                return Optional.empty();
            }
            e.printStackTrace();
            throw e;
        }


    }


}
