package com.example.review_service.entity;

import com.example.review_service.enumeration.UsagePeriod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review {

    private static final String SEQ_NAME = "review_seq";


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private Long productId;

    @Column(columnDefinition="TEXT")
    private String comment;

    @Column(columnDefinition="TEXT")
    private String advantages;

    @Column(columnDefinition="TEXT")
    private String disAdvantages;

    @NotNull
    private Integer rating;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UsagePeriod usagePeriod;

    @NotNull
    private boolean anonymousReview;

    @ElementCollection
    private List<String> photoFileNames = new ArrayList<>();

    @NotNull
    private LocalDateTime dateOfCreation;

    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }






}
