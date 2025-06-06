package com.example.managerapp.controller;

import com.example.managerapp.rest.BucketRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller

public class BucketController {
   // private final BucketRestClient bucketRestClient;

/*    @Autowired
    public BucketController( BucketRestClient bucketRestClient) {
        this.bucketRestClient = bucketRestClient;
    }*/


   @GetMapping("/get_bucket")
    public String aboutBucket(Model model, Principal principal){
      /*  if(principal== null) return "redirect:/login";

        BucketDTO bucketDTO = bucketService.getBucketDtoByUser(principal.getName());
        model.addAttribute("bucket",bucketDTO);*/
        return "bucket"; // todo






    }

















}
