package com.example.order_service.controller.grpc;

import com.example.common.grpc.delivery.Delivery;
import com.example.common.grpc.delivery.DeliveryServiceGrpc;
import com.example.common.grpc.user.User;
import com.example.order_service.entity.DeliveryInfo;
import com.example.order_service.service.DeliveryInfoService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderGrpcService extends DeliveryServiceGrpc.DeliveryServiceImplBase{

    private final DeliveryInfoService deliveryInfoService;

    @Override
    public void getUserAddress(Delivery.IdRequest request, StreamObserver<Delivery.AddressResponse> responseObserver) {

        Optional<DeliveryInfo> deliveryOptional = deliveryInfoService.getDeliveryInfo(request.getUserEntityId());

        if(deliveryOptional.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            return;
        }
        DeliveryInfo deliveryInfo = deliveryOptional.get();

        Delivery.AddressResponse addressResponse =
                Delivery.AddressResponse.newBuilder()
                        .setFullAddress(deliveryInfo.getUserAddress())
                        .setLatitude(deliveryInfo.getAddressLatitude())
                        .setLongitude(deliveryInfo.getAddressLongitude())

                .build();

        responseObserver.onNext(addressResponse);
        responseObserver.onCompleted();


    }






}
