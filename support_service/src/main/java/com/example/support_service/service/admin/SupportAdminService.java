package com.example.support_service.service.admin;

import com.example.common.exception.EntityNotFoundException;
import com.example.support_service.entity.SupportChat;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.repository.SupportChatRepository;
import com.example.support_service.repository.SupportMessageRepository;
import lombok.NonNull;
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

    public int countAllActiveSupportChats(){
        return supportChatRepository.countAllActiveChats();

    }

    public List<SupportChat> getAllUserSupportChats(long userId){

        return supportChatRepository.findAllByUserIdOrderByIdDesc(userId);
    }

    public List<SupportMessage> getAllSupportChatMessages(long chatId){

        SupportChat supportChat = supportChatRepository.findById(chatId)
                .orElseThrow(()->new EntityNotFoundException(SupportChat.class,chatId));

        return supportMessageRepository.findAllByChatOrderById(supportChat);

    }

    public SupportMessage saveSupportMessage(long chatId, @NonNull String message) {

        SupportChat supportChat = supportChatRepository.findById(chatId)
                .orElseThrow(()->new EntityNotFoundException(SupportChat.class,chatId));

        supportChat.setNeedsAnswer(false);
        supportChat.setContainsMessages(true);
        supportChat.setRead(false);


        SupportMessage supportMessage = new SupportMessage();

        supportMessage.setMessage(message);
        supportMessage.setUserMessage(false);
        supportMessage.setChat(supportChat);

        supportChatRepository.save(supportChat);

        return supportMessageRepository.save(supportMessage);


    }









}
