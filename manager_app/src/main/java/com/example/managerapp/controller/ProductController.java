package com.example.managerapp.controller;



import com.example.managerapp.exception.BadRequestException;
import com.example.managerapp.rest.ProductRestClient;
import com.example.managerapp.record.Product;
import com.example.managerapp.rest.UserRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private void createProduct(Model model, Principal principal, @RequestParam("file1") MultipartFile file1,
                               @RequestParam("file2") MultipartFile file2,
                               @RequestParam("file3") MultipartFile file3,
                               Product product) throws IOException {
       try{
           productRestClient.createProduct(principal, product, file1, file2, file3);
       } catch (BadRequestException e) {
           model.addAttribute("errors", e.getErrors());
       }

    }


    @GetMapping("/products")
    private String list(Principal principal, Model model, @RequestParam(name="title",required = false) String title) {
        List<Product> list = productRestClient.getAllProducts(title);
        model.addAttribute("products",list);
        model.addAttribute("title",title);
        return "products";



    }
    @GetMapping("/product_to_bucket/{id}")
    private String addToBucket(@PathVariable long id, Principal principal) {
       /* if (principal == null) return "redirect:/login";

        productService.addToUserBucket(id, principal.getName());*/
        return "redirect:/products";

    }

    @GetMapping("/product_info/{id}")
    private String productInfo(@PathVariable long id, Model model){
       /* Optional<Product> product = productService.getProductByID(id);
        if(product.isEmpty()) return "redirect:/products";
        model.addAttribute("product",productMapper.toProductDTO(product.get()));
        model.addAttribute("images",product.get().getImages());*/
        return "product-info";

    }
















}
