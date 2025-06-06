package com.example.catalogue_service.mapper;

import com.example.catalogue_service.dto.ProductDTO;
import com.example.catalogue_service.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductDTO product);

    ProductDTO toProductDTO(Product product);

    List<ProductDTO> toProductDTOList(List<Product> productList);

    List<Product> toProductList(List<ProductDTO> productDTOList);

}
