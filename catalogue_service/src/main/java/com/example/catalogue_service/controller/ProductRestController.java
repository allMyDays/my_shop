package com.example.catalogue_service.controller;

import com.example.catalogue_service.dto.GetProductDTO;
import com.example.catalogue_service.dto.SendProductDTO;
import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.mapper.ProductMapper;
import com.example.catalogue_service.service.ProductService;
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
    public SendProductDTO getProduct(@ModelAttribute("product") Product product) {
        return productMapper.toSendProductDTO(product);

    }

    @PatchMapping
    public ResponseEntity<?> updateProduct(@PathVariable("productId") long productId,
                                              @Valid @RequestBody GetProductDTO productDTO,
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


