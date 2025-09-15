package com.example.catalogue_service.controller.rest;

import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.mapper.LocalProductMapper;
import com.example.catalogue_service.service.ProductService;
import com.example.common.dto.product.ProductResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/catalogue/products/{productId:\\d+}")
public class ProductRestController {

    private final ProductService productService;

    private final LocalProductMapper productMapper;

    @ModelAttribute("product")
    public Product getProduct(@PathVariable("productId") Long productId) {
        return this.productService.getProductByID(productId)
                .orElseThrow(()-> new NoSuchElementException("no product found with id: " + productId));
    }

    @GetMapping
    public ProductResponseDTO getProduct(@ModelAttribute("product") Product product) {
        return productMapper.toResponseProductDTO(product);

    }

  /*  @PatchMapping
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

    }*/
    @DeleteMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();






    }






}


