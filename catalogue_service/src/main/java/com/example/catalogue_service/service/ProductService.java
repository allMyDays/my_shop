package com.example.catalogue_service.service;


import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
//@Slf4j
public class ProductService {//implements com.example.catalogue_service.service.i.ProductService {

    private final ProductRepository productRepository;



    public List<Product> getAll(Long categoryId, String title) {

        return productRepository.findByCategoryAndTitle(categoryId, title);
    }

    public List<Product> getProductsByIDs(List<Long> ids) {

        return productRepository.findAllByIdIn(ids);
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
