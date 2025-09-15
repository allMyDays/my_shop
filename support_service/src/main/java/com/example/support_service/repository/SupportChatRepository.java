package com.example.support_service.repository;
import com.example.support_service.entity.SupportChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportChatRepository extends JpaRepository<SupportChat, Long> {

    List<SupportChat> findAllByUserIdOrderByIdDesc(Long userId);

    List<SupportChat> findAllByNeedsAnswerTrueOrderByIdAsc();

}
