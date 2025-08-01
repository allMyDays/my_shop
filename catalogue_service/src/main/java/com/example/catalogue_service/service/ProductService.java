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


    public Product createProduct(Principal principal, Product product, MultipartFile file1, MultipartFile file2, MultipartFile file3) {
       /* Image image1;
        Image image2;
        Image image3;
       // product.setCreator(userService.findByPrincipal(principal));

        if(file1!=null){
            image1 = toImage(file1);
            image1.setPreviewImage(true);
            product.addImageToProduct(image1);
        }
        if(file2!=null){
            image2 = toImage(file2);
            product.addImageToProduct(image2);
        }
        if(file3!=null){
            image3 = toImage(file3);
            product.addImageToProduct(image3);
        }

     //   log.info("Saving new product. Title:{}",product.getTitle());
        Product productFromBD = productRepository.save(product);
        productFromBD.setPreviewImageID(productFromBD.getImages().isEmpty()?null:productFromBD.getImages().get(0).getId());
        return productRepository.save(productFromBD);*/
        return null;

    }


    public Product updateProduct(Long productID, Product product, MultipartFile file1, MultipartFile file2, MultipartFile file3) {
       //todo
        return product;
    }


    public void deleteProduct(long productId) {
        //todo
    }

  /*  private Image toImage(MultipartFile file){
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        try {
            image.setBytes(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;

    }*/



}
