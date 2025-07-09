package com.example.catalogue_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Category {

    private static final String SEQ_NAME = "category_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;











}
