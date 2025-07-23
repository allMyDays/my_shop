package com.example.managerapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WishList {

    @Id
    @EqualsAndHashCode.Include
    private Long userID;

    @ElementCollection
    private List<Long> productIDs = new ArrayList<>();




}
