package com.example.catalogue_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    private static final String SEQ_NAME = "product_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    private Long id;

    @Column
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private int price;

   /* @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="products_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;*/

    @Column
    private LocalDateTime dateOfCreation;

    @Column
    private Long previewImageID;

    /*@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "product")
    private List<Image> images = new ArrayList<>();*/

   /* @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn
    private MyUser creator;*/

    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }

   /* public void addImageToProduct(Image image){
        image.setProduct(this);
        images.add(image);
    }*/













}