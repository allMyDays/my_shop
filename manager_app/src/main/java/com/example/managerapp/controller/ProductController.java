package com.example.managerapp.controller;



import com.example.managerapp.controller.payload.NewProductPayload;
import com.example.managerapp.entity.ProductRecord;
import com.example.managerapp.exception.BadRequestException;
import com.example.managerapp.rest.ProductRestClient;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProductController {


    private final ProductRestClient productRestClient;




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


    @GetMapping("/products_page")
    public String list(Model model, @RequestParam(name="filter",required = false) String filter, @RequestParam(name = "categoryId",required = false) Long categoryId) {
        List<ProductRecord> list = productRestClient.getAllProducts(categoryId,filter);
        model.addAttribute("products",list);
        model.addAttribute("filter",filter);
        model.addAttribute("selectedCategoryId",categoryId);
        return "products";
    }
    @GetMapping("/product_create")
    public String newProductPage(){
        return "catalogue/products/new_product";
    }



    @GetMapping("/product_page/{id:\\d+}")
    public String productInfo(@PathVariable Long id, Model model){
        Optional<ProductRecord> productRecord = productRestClient.getProductByID(id);
        if(productRecord.isEmpty()) return "redirect:/products_page";
        model.addAttribute("product",productRecord.get());
        return "product";

    }
















}
