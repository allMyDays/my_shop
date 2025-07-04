package com.example.managerapp.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CartItem {


    private static final String SEQ_NAME = "cartItem_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name = SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    private Long id;


    private Long productId;

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;





}
