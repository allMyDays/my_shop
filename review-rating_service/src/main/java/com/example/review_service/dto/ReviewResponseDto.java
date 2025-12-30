package com.example.review_service.dto;
import com.example.common.security.XssSanitizer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
//@Setter
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReviewResponseDto {

    @Setter
    private Long id;

    @Setter
    private Long userId;


    private String advantages;

    private String disAdvantages;

    private String comment;

    @Setter
    private String usagePeriod;

    @Setter
    private Integer rating;

    @Setter
    private boolean anonymousReview;

    @Setter
    private List<String> photoFileNames = new ArrayList<>();

    @Setter
    private LocalDateTime dateOfCreation;

    private String userVisibleName;


    @Setter
    private String userAvatarFileName;

    @Setter
    private LocalDateTime dateOfLastEditing;

    @Setter
    private int editingQuantity=0;


    public void setAdvantages(String advantages) {
        this.advantages = XssSanitizer.sanitize(advantages);
    }

    public void setDisAdvantages(String disAdvantages) {
        this.disAdvantages = XssSanitizer.sanitize(disAdvantages);
    }

    public void setComment(String comment) {
        this.comment = XssSanitizer.sanitize(comment);
    }

    public void setUserVisibleName(String userVisibleName) {
        this.userVisibleName = XssSanitizer.sanitize(userVisibleName);
    }









}
