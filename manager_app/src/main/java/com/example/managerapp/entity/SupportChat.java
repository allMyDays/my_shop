package com.example.managerapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class SupportChat {
    private static final String SEQ_NAME = "support_chat_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name= SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    private Long id;

    private LocalDateTime dateOfCreation;

    private String topic;


    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SupportMessage> messages = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "user_id")
    private MyUser user;

    @Setter
    boolean needsAnswer;


    @PrePersist
    private void init(){
        dateOfCreation = LocalDateTime.now();

    }


}
