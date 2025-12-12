package com.example.catalogue_service.entity;

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
@Table(name = "products")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {

    private static final String SEQ_NAME = "product_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(columnDefinition="VARCHAR")
    @NotNull
    private String title;

    @Column(columnDefinition = "text")
    @NotNull
    private String description;

    @Column
    @NotNull
    private int price;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="products_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;

    @Column
    @NotNull
    private LocalDateTime dateOfCreation;

    @Column
    @NotNull
    private String previewImageFileName;

    @ElementCollection
    private List<String> imageFileNames = new ArrayList<>();

    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }

}