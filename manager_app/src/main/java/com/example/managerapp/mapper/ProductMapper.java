package com.example.managerapp.mapper;

import com.example.catalogue.grpc.Category;
import com.example.catalogue.grpc.Product;
import com.example.catalogue.grpc.ProductResponse;
import com.example.managerapp.dto.category.CategoryResponseDTO;
import com.example.managerapp.dto.product.ProductResponseDTO;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring",uses = {CategoryMapper.class})
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
