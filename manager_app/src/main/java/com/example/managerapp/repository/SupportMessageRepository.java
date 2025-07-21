package com.example.managerapp.repository;

import com.example.managerapp.entity.MyUser;
import com.example.managerapp.entity.SupportChat;
import com.example.managerapp.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findAllByChatOrderById(SupportChat chat);

}
