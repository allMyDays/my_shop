package com.example.managerapp.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter
public class SupportMessage {

    private static final String SEQ_NAME = "support_message_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    private LocalDateTime dateOfCreation;

    @Column(columnDefinition="TEXT")
    private String message;

    private boolean isUserMessage;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private SupportChat chat;


















    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }







}
