package com.example.order_service.mapper;

import com.example.order_service.dto.DeliveryInfoDto;
import com.example.order_service.entity.DeliveryInfo;

import org.mapstruct.Mapper;


import static com.example.common.service.CommonProductService.formatPrice;
import static com.example.order_service.service.DeliveryInfoService.*;


@Mapper(componentModel = "spring")
public abstract class DeliveryInfoMapper {
    
    
    public abstract DeliveryInfoDto toDeliveryInfoWithNoAdditions(DeliveryInfo deliveryInfo);


    public DeliveryInfoDto toDeliveryInfoDto(DeliveryInfo deliveryInfo){

        DeliveryInfoDto deliveryInfoDto = toDeliveryInfoWithNoAdditions(deliveryInfo);
        deliveryInfoDto.setStorageAddress(STORAGE_ADDRESS);
        deliveryInfoDto.setDeliveryStraightDistance(calculateStraightDistance(deliveryInfo.getAddressLatitude(), deliveryInfo.getAddressLongitude()));
        deliveryInfoDto.setDeliveryTimeView(formatDuration(deliveryInfo.getDeliveryTime()));
        deliveryInfoDto.setDeliveryDistanceView(formatDistance(deliveryInfo.getDeliveryDistance()));
        deliveryInfoDto.setDeliveryStraightDistanceView(formatDistance(deliveryInfoDto.getDeliveryStraightDistance()));
        deliveryInfoDto.setDeliveryPrice(calculateDeliveryPrice(deliveryInfoDto.getDeliveryDistance(), deliveryInfo.getDeliveryTime()));
        deliveryInfoDto.setDeliveryPriceView(formatPrice(deliveryInfoDto.getDeliveryPrice()));

        return deliveryInfoDto;

    }





}
