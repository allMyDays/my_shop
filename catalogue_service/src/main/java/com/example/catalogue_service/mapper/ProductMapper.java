package com.example.catalogue_service.mapper;

import com.example.catalogue_service.dto.GetProductDTO;
import com.example.catalogue_service.dto.SendProductDTO;
import com.example.catalogue_service.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    SendProductDTO toSendProductDTO(Product product);

    Product toProduct(GetProductDTO sendProductDTO);

    List<SendProductDTO> toSendProductDTOList(List<Product> all);


}
