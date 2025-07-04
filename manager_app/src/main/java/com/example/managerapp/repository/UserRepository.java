package com.example.managerapp.repository;

import com.example.managerapp.entity.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<MyUser, Long> {

    public Optional<MyUser> findByKeycloakID(String key);




}
