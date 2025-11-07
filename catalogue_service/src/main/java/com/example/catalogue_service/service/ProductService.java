package com.example.catalogue_service.service;


import com.example.catalogue_service.entity.Product;
import com.example.catalogue_service.repository.ProductRepository;
import com.example.common.dto.product.ProductIdAndPriceDto;
import com.example.common.dto.product.ProductIdAndQuantityDto;
import com.example.common.exception.ProductNotFoundException;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProductService {

    private final ProductRepository productRepository;

    public Stream<Product> getAll(Long categoryId, @NonNull String title, int offset) {

        if(categoryId==null||categoryId==0){
            categoryId=null;
        }
        if(title.length()<2){
            return Stream.empty();
        }

        int limit = 40;

        int page = offset/limit;


        return productRepository.findByTitleAndOptionalCategory(title, categoryId, PageRequest.of(page, limit));
    }

    @Transactional()
    public Stream<Product> getProductsByIDs(@NonNull List<Long> ids) {


        return productRepository.findAllByIdIn(ids);
    }

    public Optional<Product> getProductByID(long productID) {
        return productRepository.findById(productID);
    }


    public boolean productExists(long productID) {
        return productRepository.existsById(productID);
    }


    public List<Long> productsExist(@NonNull List<Long> productIDs) {
     try {
         return productRepository.findProductIdsByIdIn(
                 productIDs.stream()
                         .filter(a -> a > 0)
                         .toList()
         );

     }catch (Exception e){
         e.printStackTrace();
         throw e;
     }
    }

    public void deleteProductImage(long productID, @NonNull String fileName, boolean previewImage) {

        Optional<Product> product = productRepository.findById(productID);
        if(product.isPresent()){
            Product product1 = product.get();

            if(previewImage){
                product1.setPreviewImageFileName(null);
            }else{
                product1.getImageFileNames().removeIf(f->f.equalsIgnoreCase(fileName));
            }
            productRepository.save(product1);
        }else throw new ProductNotFoundException(List.of(productID));
    }

    public void setProductImage(long productID, @NonNull String fileName, boolean previewImage) {
        Optional<Product> product = productRepository.findById(productID);
        if(product.isPresent()){
            Product product1 = product.get();
            if(previewImage){
                product1.setPreviewImageFileName(fileName);
            }else{
                product1.getImageFileNames().add(fileName);
            }
            productRepository.save(product1);
        }else throw new ProductNotFoundException(List.of(productID));
    }

    public int getTotalPrice(@NonNull List<ProductIdAndQuantityDto> productIdAndQuantityDtos) {

        List<Long> ids = productIdAndQuantityDtos.stream()
                .map(ProductIdAndQuantityDto::getProductId)
                .toList();

        List<Product> products = productRepository.findAllById(ids);

        return productIdAndQuantityDtos.stream()
                .mapToInt(dto-> {
                    Product product = products.stream()
                            .filter(p->
                            p.getId().equals(dto.getProductId()))
                            .findFirst()
                            .orElse(null);
                    return product==null?0:product.getPrice()*
                            dto.getProductQuantity();
                }).sum();



    }

    public List<ProductIdAndPriceDto> getProductsPrice(@NonNull List<Long> ids) {

        return productRepository.findIdAndPriceByIds(ids);

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
