package com.example.managerapp.controller;



import com.example.managerapp.controller.payload.NewProductPayload;
import com.example.managerapp.entity.ProductRecord;
import com.example.managerapp.exception.BadRequestException;
import com.example.managerapp.rest.ProductRestClient;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
           ProductRecord dataBaseProductRecord = productRestClient.createProduct(product, file1, file2, file3);
           return "redirect:/catalogue/products/%d".formatted(dataBaseProductRecord.id());
       } catch (BadRequestException exception) {
        model.addAttribute("payload", product);
        model.addAttribute("errors", exception.getErrors());
        return "catalogue/products/new_product";}  // todo

    }


    @GetMapping("/products")
    public String list(Principal principal, Model model, @RequestParam(name="filter",required = false) String filter) {
        List<ProductRecord> list = productRestClient.getAllProducts(filter);
        model.addAttribute("products",list);
        model.addAttribute("title",filter);
        return "products";



    }
    @GetMapping("/product_create")
    public String newProductPage(){
        return "catalogue/products/new_product";
    }



    @GetMapping("/product_info/{id}")
    public String productInfo(@PathVariable long id, Model model){
       /* Optional<ProductRecord> productRecord = productService.getProductByID(id);
        if(productRecord.isEmpty()) return "redirect:/products";
        model.addAttribute("productRecord",productMapper.toProductDTO(productRecord.get()));
        model.addAttribute("images",productRecord.get().getImages());*/
        return "product-info";

    }
















}
