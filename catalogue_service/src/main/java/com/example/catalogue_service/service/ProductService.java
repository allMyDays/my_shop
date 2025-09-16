package com.example.catalogue_service.service;


import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProductService {

    private final ProductRepository productRepository;



    public Stream<Product> getAll(Long categoryId, String title, int offset) {

        System.out.println("categoryId: " + categoryId);
        System.out.println("title: " + title);
        System.out.println("offset: " + offset);

        if(categoryId==0){
            categoryId=null;
        }
        if(title==null||title.length()<2){
            return Stream.empty();
        }

        int limit = 40;

        int page = offset/limit;


        return productRepository.findByTitleAndOptionalCategory(title, categoryId, PageRequest.of(page, limit));
    }

    public Stream<Product> getProductsByIDs(List<Long> ids, int limit, int offset) {

        int page = offset/limit;

        return productRepository.findAllByIdIn(ids, PageRequest.of(page, limit));
    }





    public Optional<Product> getProductByID(Long productID) {
        return productRepository.findById(productID);
    }

    public void deleteProductImage(Long productID, String fileName, boolean previewImage) {
        if(fileName==null){
            throw new NullPointerException("fileName is null");
        }

        Optional<Product> product = productRepository.findById(productID);
        if(product.isPresent()){
            Product product1 = product.get();

            if(previewImage){
                product1.setPreviewImageFileName(null);
            }else{
                product1.getImageFileNames().removeIf(f->f.equalsIgnoreCase(fileName));
            }
            productRepository.save(product1);
        }
    }

    public void setProductImage(Long productID, String fileName, boolean previewImage) {
        if(fileName==null){
            throw new NullPointerException("fileName is null");
        }
        Optional<Product> product = productRepository.findById(productID);
        if(product.isPresent()){
            Product product1 = product.get();
            if(previewImage){
                product1.setPreviewImageFileName(fileName);
            }else{
                product1.getImageFileNames().add(fileName);
            }
            productRepository.save(product1);
        }
    }



    public Product createProduct() {
       //todo
        return null;
    }


    public Product updateProduct() {
       //todo
        return null;
    }


    public void deleteProduct(long productId) {
        //todo
    }




}
