package com.example.managerapp.controller;



import com.example.managerapp.controller.payload.NewProductPayload;
import com.example.managerapp.exception.BadRequestException;
import com.example.managerapp.rest.ProductRestClient;
import com.example.managerapp.record.Product;
import com.example.managerapp.rest.UserRestClient;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
public class ProductController {

    private final ProductRestClient productRestClient;

    @Autowired
    public ProductController(ProductRestClient productRestClient) {
        this.productRestClient = productRestClient;
    }
    // private final UserRestClient userRestClient;


    @PreAuthorize("hasAuthority('SELLER')")
    @PostMapping("/product_create")
    public String createProduct(NewProductPayload product,
                               Model model,
                                @RequestParam("file1") MultipartFile file1,
                                @RequestParam("file2") MultipartFile file2,
                                @RequestParam("file3") MultipartFile file3,
                                HttpServletResponse response
                               ) {
       try{
           Product dataBaseProduct = productRestClient.createProduct(product, file1, file2, file3);
           return "redirect:/catalogue/products/%d".formatted(dataBaseProduct.id());
       } catch (BadRequestException exception) {
        model.addAttribute("payload", product);
        model.addAttribute("errors", exception.getErrors());
        return "catalogue/products/new_product";}  // todo

    }


    @GetMapping("/products")
    public String list(Principal principal, Model model, @RequestParam(name="filter",required = false) String filter) {
        List<Product> list = productRestClient.getAllProducts(filter);
        model.addAttribute("products",list);
        model.addAttribute("title",filter);
        return "products";



    }
    @GetMapping("/product_create")
    public String newProductPage(){
        return "catalogue/products/new_product";
    }

    @GetMapping("/product_to_bucket/{id}")
    public String addToBucket(@PathVariable long id, Principal principal) {
       /* if (principal == null) return "redirect:/login";

        productService.addToUserBucket(id, principal.getName());*/
        return "redirect:/products";

    }

    @GetMapping("/product_info/{id}")
    public String productInfo(@PathVariable long id, Model model){
       /* Optional<Product> product = productService.getProductByID(id);
        if(product.isEmpty()) return "redirect:/products";
        model.addAttribute("product",productMapper.toProductDTO(product.get()));
        model.addAttribute("images",product.get().getImages());*/
        return "product-info";

    }
















}
