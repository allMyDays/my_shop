package com.example.managerapp.repository;

import com.example.managerapp.entity.MyUser;
import com.example.managerapp.entity.SupportChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SupportChatRepository extends JpaRepository<SupportChat, Long> {

    List<SupportChat> findAllByUserOrderByIdDesc(MyUser user);

    List<SupportChat> findAllByNeedsAnswerTrueOrderByIdAsc();

}
