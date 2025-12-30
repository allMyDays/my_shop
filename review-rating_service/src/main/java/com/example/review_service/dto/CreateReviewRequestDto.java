package com.example.review_service.dto;

import com.example.common.security.XssSanitizer;
import com.example.review_service.enumeration.UsagePeriod;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CreateReviewRequestDto {

    @NotNull
    @Positive(message = "productId must be positive")
    private Long productId;

    @Size(min = 5, max = 250, message = "Текст достоинств товара должен иметь длину от 5 до 250 символов.")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "В тексте достоинств были обнаружены запрещенные символы."
    )
    private String advantages;

    @Size(min = 5, max = 250, message = "Текст недостатков товара должен иметь длину от 5 до 250 символов.")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "В тексте недостатков были обнаружены запрещенные символы."
    )
    private String disAdvantages;

    @Size(min = 5, max = 500, message = "Комментарий отзыва должен иметь длину от 5 до 500 символов.")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "В тексте комментариев были обнаружены запрещенные символы."
    )
    private String comment;

    @NotNull(message = "usagePeriod is required")
    private UsagePeriod usagePeriod;


    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private boolean anonymousReview;

}
