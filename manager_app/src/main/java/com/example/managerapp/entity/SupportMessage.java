package com.example.managerapp.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
@Setter
public class SupportMessage {

    private static final String SEQ_NAME = "support_message_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
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
