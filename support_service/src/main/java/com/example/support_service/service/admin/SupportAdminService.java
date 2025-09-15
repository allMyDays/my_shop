package com.example.support_service.service.admin;

import com.example.support_service.entity.SupportChat;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.repository.SupportChatRepository;
import com.example.support_service.repository.SupportMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportAdminService {

    private final SupportChatRepository supportChatRepository;

    private final SupportMessageRepository supportMessageRepository;



    public List<SupportChat> getAllActiveSupportChats(){

        return supportChatRepository.findAllByNeedsAnswerTrueOrderByIdAsc();

    }

    public List<SupportChat> getAllUserSupportChats(Long userId){

        if(userId==null){
            throw new RuntimeException("user id is null");
        }

        return supportChatRepository.findAllByUserIdOrderByIdDesc(userId);
    }

    public List<SupportMessage> getAllSupportChatMessages(Long chatId){

        SupportChat supportChat = supportChatRepository.findById(chatId).orElseThrow(()->new RuntimeException("chat not found"));

        return supportMessageRepository.findAllByChatOrderById(supportChat);

    }









}
