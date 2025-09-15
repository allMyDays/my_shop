package com.example.support_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SupportChat {
    private static final String SEQ_NAME = "support_chat_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    private LocalDateTime dateOfCreation;

    @NotNull
    private String topic;


    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SupportMessage> messages = new ArrayList<>();

    @NotNull
    private Long userId;

    @NotNull
    @Setter
    boolean needsAnswer;


    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }


}
