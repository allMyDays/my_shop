package com.example.support_service.repository;
import com.example.support_service.entity.SupportChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupportChatRepository extends JpaRepository<SupportChat, Long> {

    List<SupportChat> findAllByUserIdOrderByIdDesc(Long userId);

    List<SupportChat> findAllByNeedsAnswerTrueOrderByIdAsc();

    @Query("SELECT COUNT(s) FROM SupportChat s WHERE s.isRead=false AND s.userId = :userId")
    int countUnreadChatsByUserId(@Param("userId") Long userId);

    @Query("SELECT s.id FROM SupportChat s WHERE s.needsAnswer = true AND s.userId = :userId")
    List<Long> findIdsWhereNeedsAnswerTrueByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM SupportChat s WHERE s.needsAnswer = true")
    int countAllActiveChats();


}
