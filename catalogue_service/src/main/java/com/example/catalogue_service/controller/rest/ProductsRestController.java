package com.example.catalogue_service.controller.rest;

import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.mapper.LocalProductMapper;
import com.example.catalogue_service.service.ProductService;
import com.example.common.dto.product.PriceDto;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.enumeration.category.CategoryCode;
import com.example.common.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

import static com.example.common.service.CommonProductService.formatPrice;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/catalogue/products")
public class ProductsRestController {
     private final ProductService productService;

     private final LocalProductMapper productMapper;

    @GetMapping
    public List<ProductResponseDTO> getProducts(@RequestParam(required = false) CategoryCode categoryCode, @RequestParam(required = false) String filter, @RequestParam int offset) {

        try(Stream<Product> productStream = productService.getAll(categoryCode,filter,offset)){
        return productMapper.toResponseProductDTOList(productStream.toList());
        }

    }

    @GetMapping("/{productId:\\d+}")
    public ProductResponseDTO getProduct(@PathVariable long productId) {
        Product product = productService.getProductByID(productId)
                .orElseThrow(()-> new ProductNotFoundException(List.of(productId)));
        return productMapper.toResponseProductDTO(product);

    }

    @PostMapping("/get-by-ids")
    public List<ProductResponseDTO> getProductsByIDs(@RequestBody List<Long> IDs) {

            try (Stream<Product> productStream = productService.getProductsByIDs(IDs)) {
                List<Product> products = productStream.toList();
                return productMapper.toResponseProductDTOList(products);
            }



    }

    @PostMapping("/total-price")
    public PriceDto getTotalPrice(@RequestBody List<ProductIdAndQuantityDto> productIdAndQuantityDto) {
        int totalPrice = productService.getTotalPrice(productIdAndQuantityDto);
        return new PriceDto(totalPrice,formatPrice(totalPrice));
    }


}
