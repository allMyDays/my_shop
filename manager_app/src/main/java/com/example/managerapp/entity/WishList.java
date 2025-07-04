package com.example.managerapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishList {

    @Id
    private Long userID;

    @ElementCollection
    private List<Long> productIDs = new ArrayList<>();




}
