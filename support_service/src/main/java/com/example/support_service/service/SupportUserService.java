package com.example.support_service.service;

import com.example.common.client.kafka.EmailKafkaClient;
import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.TooManyFunctionCallsException;
import com.example.common.exception.UserNotFoundException;
import com.example.common.exception.UserNotOwnerException;
import com.example.support_service.entity.SupportChat;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.repository.SupportChatRepository;
import com.example.support_service.repository.SupportMessageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.example.support_service.enumeration.RedisSubKeys.SUPPORT_CHAT_CREATION_LIMITED;
import static com.example.support_service.enumeration.RedisSubKeys.SUPPORT_MESSAGE_SENDING_LIMITED;

@Service
@RequiredArgsConstructor
public class SupportUserService {

    private final RedisService redisService;

    private final SupportChatRepository supportChatRepository;

    private final SupportMessageRepository supportMessageRepository;

    private final EmailKafkaClient emailKafkaClient;


    @Value("${support_agent.email}")
    private String agent_email;


    public Long createSupportChat(long userId, @NonNull String topic) {

         if(chatCreationIsLimited(userId)){
             throw new TooManyFunctionCallsException();
         }

         SupportChat supportChat = new SupportChat();
         supportChat.setUserId(userId);
         supportChat.setTopic(topic);
         supportChat.setContainsMessages(false);
         supportChat.setNeedsAnswer(false);
         supportChat = supportChatRepository.save(supportChat);

        Optional<String> keyOpt = redisService.get(SUPPORT_CHAT_CREATION_LIMITED+":"+userId);

         redisService.saveTemp(SUPPORT_CHAT_CREATION_LIMITED+":"+userId, keyOpt.map(s -> String.valueOf(Integer.parseInt(s) + 1)).orElse("1"),7200);

         return supportChat.getId();

    }

    public boolean chatCreationIsLimited(long userId) {

        Optional<String> keyOpt = redisService.get(SUPPORT_CHAT_CREATION_LIMITED+":"+userId);
        if(keyOpt.isEmpty()) return false;

        int key = Integer.parseInt(keyOpt.get());

        return key >= 5;
    }
    public boolean messageSendingIsLimited(long chatId){

        Optional<String> keyOpt = redisService.get(SUPPORT_MESSAGE_SENDING_LIMITED+":"+chatId);
        if(keyOpt.isEmpty()) return false;

        int key = Integer.parseInt(keyOpt.get());

        return key >= 4;

    }

    public SupportMessage saveSupportMessage(long userId, long chatId, @NonNull String message) {

            SupportChat supportChat = validateEntityAndOwnership(userId, chatId);

            Optional<String> keyOpt = redisService.get(SUPPORT_MESSAGE_SENDING_LIMITED+":"+chatId);

            redisService.saveTemp(SUPPORT_MESSAGE_SENDING_LIMITED+":"+chatId,
                    keyOpt.map(s -> String.valueOf(Integer.parseInt(s) + 1)).orElse("1"), 120);
            supportChat.setNeedsAnswer(true);
            supportChat.setContainsMessages(true);
            emailKafkaClient.sendSimpleMail(
                    agent_email,
                    "Новое сообщение в чат поддержки № %d".formatted(chatId),"Сообщение:\n\n %s".formatted(message)
            );



        SupportMessage supportMessage = new SupportMessage();


        supportMessage.setMessage(message);
        supportMessage.setUserMessage(true);
        supportMessage.setChat(supportChat);

        supportChatRepository.save(supportChat);

        return supportMessageRepository.save(supportMessage);


    }

    public List<SupportChat> getAllUserSupportChats(long userId){

        return supportChatRepository.findAllByUserIdOrderByIdDesc(userId);
    }



    public List<SupportMessage> getAllSupportChatMessages(long userId, long chatId) {

        SupportChat supportChat = validateEntityAndOwnership(userId, chatId);

        return supportMessageRepository.findAllByChatOrderById(supportChat);

    }

    public void deleteSupportChat(long userId, long chatId) throws UserNotFoundException {

        SupportChat supportChat = validateEntityAndOwnership(userId, chatId);

        supportChatRepository.delete(supportChat);

    }

    public SupportChat validateEntityAndOwnership(long userId, long chatId) {

        SupportChat supportChat = supportChatRepository.findById(chatId)
                .orElseThrow(()->new EntityNotFoundException(SupportChat.class,chatId));

        if(!supportChat.getUserId().equals(userId)){
            throw new UserNotOwnerException(userId,supportChat.getUserId(),SupportChat.class);
        }  return supportChat;




    }
}















