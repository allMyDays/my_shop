package com.example.frontend.controller;

import com.example.common.client.grpc.ProductGrpcClient;
import com.example.common.dto.product.rest.ProductResponseDTO;
import com.example.common.enumeration.category.CategoryCode;
import com.example.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

import static com.example.common.service.CommonProductService.ARTICLE_PATTERN;
import static com.example.common.service.CommonProductService.extractProductId;
import static com.example.common.service.CommonUserService.getMyUserEntityId;
import static com.example.common.service.CommonUserService.userIsAdminOrSupportAgent;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductGrpcClient productGrpcClient;

    private final Environment environment;

    @GetMapping("/get/all")
    public String productsPage(Model model, @RequestParam(required = false) String filter, @RequestParam(required = false) CategoryCode categoryCode, @AuthenticationPrincipal Jwt jwt) {
        if(filter != null && filter.trim().matches(ARTICLE_PATTERN)) {
            return productPage(extractProductId(filter.trim()), model,jwt);
        }
        if(filter!=null) model.addAttribute("filter",filter);
        if(categoryCode!=null) model.addAttribute("categoryCode",categoryCode.name());

        if(jwt!=null){
            model.addAttribute("currentUserId",getMyUserEntityId(jwt));
            model.addAttribute("isUserStaff", userIsAdminOrSupportAgent(jwt));
        }
        model.addAttribute("isTestMode", environment.acceptsProfiles(Profiles.of("test")));

        return "products";
    }

    @GetMapping("/get/{id:\\d+}")
    public String productPage(@PathVariable Long id, Model model, @AuthenticationPrincipal Jwt jwt) throws UserNotFoundException {
        Optional<ProductResponseDTO> productOptional = productGrpcClient.getProductById(id);
        if(productOptional.isEmpty()) return "products";

        ProductResponseDTO product = productOptional.get();

        List<String> allProductImages = new ArrayList<>(product.getImageFileNames());
        allProductImages.add(product.getPreviewImageFileName());

        product.setImageFileNames(allProductImages);

        model.addAttribute("product",product);
        if(jwt!=null){
            model.addAttribute("currentUserId",getMyUserEntityId(jwt));
            model.addAttribute("isUserStaff", userIsAdminOrSupportAgent(jwt));
        }
        model.addAttribute("isTestMode", environment.acceptsProfiles(Profiles.of("test")));
        return "product";

    }



    @GetMapping(value = "/lazy", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter productsLazyLoad(@RequestParam(required = false) String filter, @RequestParam(required = false) CategoryCode categoryCode, @RequestParam int offset) {

        System.out.println("CATEGORYCODE: " + categoryCode);
        System.out.println("FILTER: " + filter);
        SseEmitter emitter = new SseEmitter();

        productGrpcClient.lazyLoadProductBatchStream(Optional.ofNullable(categoryCode), Optional.ofNullable(filter), offset, productResponseDTO -> {
            try {
                emitter.send(productResponseDTO);

            } catch (IOException e) {
                emitter.completeWithError(e);
            }


        }, emitter::complete);


        return emitter;
    }






}
