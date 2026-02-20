package com.example.support_service.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter
@Getter
public class SupportMessage {

    private static final String SEQ_NAME = "support_message_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateOfCreation;

    @NotNull
    @Column(columnDefinition="TEXT", nullable = false)
    private String message;

    @NotNull
    @Column(columnDefinition="TEXT", nullable = false)
    private boolean isUserMessage;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private SupportChat chat;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="support_message_photo")
    private List<String> photoFileNames;


    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }







}
