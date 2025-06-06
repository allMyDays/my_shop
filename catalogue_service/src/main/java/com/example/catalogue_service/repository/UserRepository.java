package com.example.catalogue_service.repository;

import com.example.catalogue_service.entity.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<MyUser,Long> {
    Optional<MyUser> findByEmail(String email);
}