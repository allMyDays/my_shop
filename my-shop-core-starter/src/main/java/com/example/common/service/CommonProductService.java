package com.example.common.service;

import org.mapstruct.Named;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class CommonProductService {

    public static String ARTICLE_PATTERN = "\\d{9}";

    public static String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("ru", "RU"));
        return formatter.format(price) + "₽";
    }
    public static String generateArticle(long productId){
        return String.format("%09d", productId);
    }

    public static long extractProductId(String article){
        if (article==null||!article.matches(ARTICLE_PATTERN)){
            throw new IllegalArgumentException("Некореектный артикул: "+article);
        } return Long.parseLong(article);


    }




}
