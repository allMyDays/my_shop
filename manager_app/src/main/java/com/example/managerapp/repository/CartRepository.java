package com.example.managerapp.repository;

import com.example.managerapp.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
}
