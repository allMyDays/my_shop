package com.example.common.mapper;

import com.example.common.dto.user.rest.UserAddressDto;
import com.example.common.grpc.delivery.Delivery;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public abstract class AddressMapper {


    public abstract UserAddressDto toAddressDto(Delivery.AddressResponse userAddressResponse);


}
