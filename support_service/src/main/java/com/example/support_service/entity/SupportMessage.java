package com.example.support_service.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

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

    @NotNull
    private LocalDateTime dateOfCreation;

    @NotNull
    @Column(columnDefinition="TEXT")
    private String message;

    @NotNull
    private boolean isUserMessage;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private SupportChat chat;


















    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }







}
