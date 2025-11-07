package com.example.order_service.entity;

import com.example.order_service.enumeration.OrderLivingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    private static final String SEQ_NAME = "order_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime dateOfCreation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderLivingStatus orderStatus;

    @Column(nullable = false)
    private Integer uniqueItemQuantity;

    @Column(nullable = false)
    private Integer commonItemQuantity;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private int deliveryPrice;



}
