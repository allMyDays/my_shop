package com.example.common.service;

import org.mapstruct.Named;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class CommonProductService {

    public static String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("ru", "RU"));
        return formatter.format(price) + "₽";
    }




}
