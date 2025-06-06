package com.example.catalogue_service.controller;

import com.example.catalogue_service.dto.ProductDTO;
import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.mapper.ProductMapper;
import com.example.catalogue_service.service.i.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
    public List<ProductDTO> findProducts(@RequestParam(name = "filter", required = false) String filter) {
        return productMapper.toProductDTOList(productService.getAll(filter));
    }
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult, UriComponentsBuilder uriBuilder) throws BindException {

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
