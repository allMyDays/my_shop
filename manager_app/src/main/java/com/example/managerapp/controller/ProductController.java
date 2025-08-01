package com.example.managerapp.controller;



import com.example.managerapp.client.grpc.ProductGrpcClient;
import com.example.managerapp.dto.product.ProductResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductGrpcClient productGrpcClient;




   /* @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/product_create")
    public String createProduct(NewProductPayload product,
                               Model model,
                                @RequestParam("file1") MultipartFile file1,
                                @RequestParam("file2") MultipartFile file2,
                                @RequestParam("file3") MultipartFile file3,
                                HttpServletResponse response
                               ) {
       try{
           ProductResponseDTO productResponseDTO = productRestClient.createProduct(product, file1, file2, file3);
           return "redirect:/catalogue/products/%d".formatted(productResponseDTO.getId());
       } catch (BadRequestException exception) {
        model.addAttribute("payload", product);
        model.addAttribute("errors", exception.getErrors());
        return "catalogue/products/new_product";}  // todo

    }*/

    @GetMapping("/products_page")
    public String productsPage(Model model, @RequestParam String filter, @RequestParam(required = false) Long categoryId) {
        model.addAttribute("filter",filter);
        return "products";
    }

    @GetMapping(value = "/products/lazy", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter productsLazyLoad(@RequestParam String filter, @RequestParam(required = false) Long categoryId, @RequestParam int offset) {
                                                                                                     // SseEmitter - технология, позволяющая серверу отрпавлять данные в браузер в реальном времени по http без необходимости постоянных запросов от клиента.

        SseEmitter emitter = new SseEmitter();

        System.out.println("offset: "+offset);
        System.out.println("category: "+categoryId);

        productGrpcClient.lazyLoadProductBatchStream(categoryId, filter, offset, productResponseDTO -> {
                          try {
                              emitter.send(productResponseDTO);

                          } catch (IOException e) {
                              emitter.completeWithError(e);
                          }


    }, emitter::complete);  // чтобы завершить работу sseEmitter


      return emitter;   // если не вернуть, spring закроет http соединение сразу после выполнения контроллера и браузер ничего не получит
    }










  /*  @GetMapping("/product_create")
    public String newProductPage(){
        return "catalogue/products/new_product";
    }*/



    @GetMapping("/product_page/{id:\\d+}")
    public String productInfo(@PathVariable Long id, Model model){
        Optional<ProductResponseDTO> productOptional = productGrpcClient.getProductById(id);
        if(productOptional.isEmpty()) return "redirect:/products_page";

        ProductResponseDTO product = productOptional.get();

        List<String> allProductImages = new ArrayList<>(product.getImageFileNames());
        allProductImages.add(product.getPreviewImageFileName());

        product.setImageFileNames(allProductImages);


        model.addAttribute("product",product);
        return "product";

    }
















}
