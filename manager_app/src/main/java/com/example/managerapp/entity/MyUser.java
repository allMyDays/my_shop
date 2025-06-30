package com.example.managerapp.entity;

import com.example.managerapp.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class MyUser {
    String id;
    String nickName;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String password;
    boolean isActive;
    LocalDateTime dateOfRegistration;
    Set<Role> roles;
    Bucket bucket;
    Image avatar;
    List<Product> createdProducts;






}
