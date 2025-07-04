package com.example.catalogue_service.service.i;

import com.example.catalogue_service.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
     List<Product> getAll(String title);

     Optional<Product> getProductByID(Long productID);

     Product createProduct(Principal principal, Product product, MultipartFile file1, MultipartFile file2, MultipartFile file3);

     Product updateProduct(Long productID, Product product, MultipartFile file1, MultipartFile file2, MultipartFile file3);

     void deleteProduct(long productId);
}
