package com.example.common.dto.product.rest;

import com.example.common.security.XssSanitizer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class NewProductRequestDTO {

    private String title;

    private String description;

    @Setter
    private int price;

    public void setTitle(String title) {
        this.title = XssSanitizer.sanitize(title);
    }

    public void setDescription(String description) {
        this.description = XssSanitizer.sanitize(description);
    }
}
