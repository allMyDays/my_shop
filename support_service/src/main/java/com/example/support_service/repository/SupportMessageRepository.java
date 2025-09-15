package com.example.support_service.repository;


import com.example.support_service.entity.SupportChat;
import com.example.support_service.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findAllByChatOrderById(SupportChat chat);

}
