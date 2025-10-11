package com.example.catalogue_service.controller.rest;

import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.mapper.LocalProductMapper;
import com.example.catalogue_service.service.ProductService;
import com.example.common.dto.product.rest.ProductResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/catalogue/products")
public class ProductsRestController {
     private final ProductService productService;

     private final LocalProductMapper productMapper;

    @GetMapping
    public List<ProductResponseDTO> findProducts(@RequestParam(required = false) Long categoryId, @RequestParam String filter, @RequestParam int offset) {

        try(Stream<Product> productStream = productService.getAll(categoryId,filter,offset)){
        return productMapper.toResponseProductDTOList(productStream.toList());
        }

    }

    @PostMapping("/get-by-ids")
    public List<ProductResponseDTO> getProductByIDs(@RequestBody List<Long> IDs, @RequestParam int offset, @RequestParam int limit) {

       try(Stream<Product> productStream = productService.getProductsByIDs(IDs,limit,offset)){
           return productMapper.toResponseProductDTOList(productStream.toList());
       }



    }

   /* @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequestDTO productDTO, BindingResult bindingResult, UriComponentsBuilder uriBuilder) throws BindException {

        if(bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        else{
            Product product = productService.createProduct(null, productMapper.toProduct(productDTO),null,null,null);

            return ResponseEntity
                    .created(uriBuilder
                            .replacePath("catalogue-api/products/{productId}")
                            .build(Map.of("productId",product.getId())))
                    .body(product);
        }

    }*/
}
