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

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("catalogue-api/products/{productId:\\d+}")
public class ProductRestController {

    private final ProductService productService;

    private final ProductMapper productMapper;

    @ModelAttribute("product")
    public Product getProduct(@PathVariable("productId") Long productId) {
        return this.productService.getProductByID(productId)
                .orElseThrow(()-> new NoSuchElementException("no product found with id: " + productId));
    }

    @GetMapping
    public ProductResponseDTO getProduct(@ModelAttribute("product") Product product) {
        return productMapper.toResponseProductDTO(product);

    }

    @PatchMapping
    public ResponseEntity<?> updateProduct(@PathVariable("productId") long productId,
                                              @Valid @RequestBody ProductRequestDTO productDTO,
                                              BindingResult bindingResult) throws BindException {

        if(bindingResult.hasErrors()){
            throw new BindException(bindingResult);
        }
        else {
            productService.updateProduct(productId, productMapper.toProduct(productDTO),null,null,null);
            return ResponseEntity.noContent()
                    .build();

        }

    }
    @DeleteMapping
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();






    }






}


