package com.example.common.security;

import org.springframework.web.util.HtmlUtils;

public final class XssSanitizer {

    private XssSanitizer() {}


    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(input);
    }




}
