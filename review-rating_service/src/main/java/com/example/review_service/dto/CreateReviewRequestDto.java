package com.example.review_service.dto;

import com.example.common.security.XssSanitizer;
import com.example.review_service.enumeration.UsagePeriod;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CreateReviewRequestDto {

    @NotNull
    @Positive(message = "productId must be positive")
    @Setter
    private Long productId;

    @Size(min = 5, max = 250, message = "Текст достоинств товара должен иметь длину от 5 до 250 символов.")
    private String advantages;

    @Size(min = 5, max = 250, message = "Текст недостатков товара должен иметь длину от 5 до 250 символов.")
    private String disAdvantages;

    @Size(min = 5, max = 500, message = "Комментарий отзыва должен иметь длину от 5 до 500 символов.")
    private String comment;

    @NotNull(message = "usagePeriod is required")
    @Setter
    private UsagePeriod usagePeriod;


    @NotNull
    @Setter
    @Min(1)
    @Max(5)
    private Integer rating;

    @Setter
    private boolean anonymousReview;

    public void setComment(String comment) {
        if (comment==null||comment.trim().isEmpty()){
            this.comment=null;
            return;
        }
        this.comment = XssSanitizer.sanitize(comment).trim();
    }

    public void setAdvantages(String advantages) {
        if (advantages==null||advantages.trim().isEmpty()){
            this.advantages=null;
            return;
        }
        this.advantages = XssSanitizer.sanitize(advantages).trim();
    }

    public void setDisAdvantages(String disAdvantages) {
        if (disAdvantages==null||disAdvantages.trim().isEmpty()){
            this.disAdvantages=null;
            return;
        }
        this.disAdvantages = XssSanitizer.sanitize(disAdvantages).trim();
    }
}
