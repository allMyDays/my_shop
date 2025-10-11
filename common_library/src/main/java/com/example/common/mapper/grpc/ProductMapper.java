package com.example.common.mapper.grpc;

import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.grpc.product.ProductResponse;
import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring",uses = {CategoryMapper.class},nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ProductMapper {

   @Autowired
   protected CategoryMapper categoryMapper;


   @Mapping(target = "imageFileNames", expression = "java(productResponse.getImageFileNamesList())")
   @Mapping(target = "dateOfCreation", expression = "java(fromProtoTimeStamp(productResponse.getDateOfCreation()))")
   public abstract ProductResponseDTO toProductResponseDTO(ProductResponse productResponse);


   public abstract List<ProductResponseDTO> toProductResponseDTOList(List<ProductResponse> productList);


   protected LocalDateTime fromProtoTimeStamp(Timestamp timestamp) {

      return LocalDateTime.ofInstant(
              Instant.ofEpochSecond(timestamp.getSeconds(),timestamp.getNanos()),
              ZoneOffset.UTC
      );


   }


}
