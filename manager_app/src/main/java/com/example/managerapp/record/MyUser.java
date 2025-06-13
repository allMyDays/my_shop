package com.example.managerapp.record;

import com.example.managerapp.record.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record MyUser (int id, String email, String phoneNumber, String name, String password, boolean isActive,
                      LocalDateTime dateOfRegistration, Set<Role> roles, Bucket bucket, Image avatar, List<Product> createdProducts) {
}
