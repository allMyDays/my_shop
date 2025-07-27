package com.example.catalogue_service.controller.rest;

import com.example.catalogue_service.dto.ProductRequestDTO;
import com.example.catalogue_service.dto.ProductResponseDTO;
import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.mapper.ProductMapper;
import com.example.catalogue_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("catalogue-api/products")
public class ProductsRestController {
     private final ProductService productService;

     private final ProductMapper productMapper;

    @GetMapping
    public List<ProductResponseDTO> findProducts(@RequestParam(name = "categoryId",required = false) Long categoryId, @RequestParam(name = "filter", required = false) String filter) {

        System.out.println("!!!!!!!!!!!!!!!"+categoryId);
        return productMapper.toResponseProductDTOList(productService.getAll(categoryId,filter));

    }

    @PostMapping("/get-by-ids")
    public List<ProductResponseDTO> getProductByIDs(@RequestBody List<Long> IDs) {

        return productMapper.toResponseProductDTOList(productService.getProductsByIDs(IDs));

    }

    @PostMapping
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

    }
}
