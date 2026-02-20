package com.example.catalogue_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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

    @ManyToMany(cascade = {})
    @JoinTable(name="products_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;

    @Column
    @NotNull
    private LocalDateTime dateOfCreation;

    @Column
    @NotNull
    @Setter
    private String previewImageFileName;

    @ElementCollection
    @Setter
    private List<String> imageFileNames = new ArrayList<>();

    @Column(unique = true, nullable = true)
    @Setter
    private String identifyingCode;

    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }

    public Product(String title, String description, int price, List<Category> categories, LocalDateTime dateOfCreation) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.categories = categories;
        this.dateOfCreation = dateOfCreation;
    }
}