package com.example.review_service.dto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReviewResponseDto {

    private Long id;

    private Long userId;

    private String advantages;

    private String disAdvantages;

    private String comment;

    private String usagePeriod;

    private Integer rating;

    private boolean anonymousReview;

    private List<String> photoFileNames = new ArrayList<>();

    private LocalDateTime dateOfCreation;

    private String userVisibleName;

    private String userAvatarFileName;

    private LocalDateTime dateOfLastEditing;

    private int editingQuantity=0;








}
