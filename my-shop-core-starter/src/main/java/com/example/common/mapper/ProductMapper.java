package com.example.common.mapper;

import com.example.common.dto.product.rest.ProductMinimalInfoResponseDto;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.grpc.product.ProductResponse;
import com.example.common.service.CommonProductService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring",uses = {CategoryMapper.class},nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ProductMapper {

   @Autowired
   protected CategoryMapper categoryMapper;


   @Mapping(target = "imageFileNames", expression = "java(productResponse.getImageFileNamesList())")
   @Mapping(target = "priceView", expression = "java(com.example.common.service.CommonProductService.formatPrice(productResponse.getPrice()))")
   @Mapping(target = "priceInt", source = "price")
   public abstract ProductResponseDTO toProductResponseDTO(ProductResponse productResponse);


   public abstract List<ProductResponseDTO> toProductResponseDTOList(List<ProductResponse> productList);


   public abstract ProductMinimalInfoResponseDto toMinimalInfoDTO(ProductResponseDTO productResponseDTO);




}
