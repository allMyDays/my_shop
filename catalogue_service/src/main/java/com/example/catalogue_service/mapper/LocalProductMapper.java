package com.example.catalogue_service.mapper;
import com.example.catalogue_service.entity.Product;
import com.example.common.grpc.product.ProductResponse;
import com.example.common.dto.product.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import com.google.protobuf.Timestamp;

@Mapper(componentModel = "spring")
public abstract class LocalProductMapper {


     public abstract ProductResponseDTO toResponseProductDTO(Product product);

     //public abstract Product toProduct(ProductRequestDTO sendProductDTO);

     public abstract List<ProductResponseDTO> toResponseProductDTOList(List<Product> all);









     @Named("toProductResponse")
     public ProductResponse toProductResponse(Product product){

       return ProductResponse.newBuilder()
                  .setId(product.getId())
                  .setTitle(product.getTitle())
                  .setDescription(product.getDescription())
                  .setPrice(product.getPrice())
                  .setPreviewImageFileName(product.getPreviewImageFileName())
                  .addAllImageFileNames(product.getImageFileNames())
                  .setDateOfCreation(toProtoTimeStamp(product.getDateOfCreation()))


                .build();

    }


    public abstract List<ProductResponse> toProductResponseList(List<Product> products);



    protected Timestamp toProtoTimeStamp(LocalDateTime localDateTime){

        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

    }







}
