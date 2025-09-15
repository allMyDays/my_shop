package com.example.support_service.service;

import com.example.common.client.kafka.EmailKafkaClient;
import com.example.common.exception.UserNotFoundException;
import com.example.support_service.entity.SupportChat;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.repository.SupportChatRepository;
import com.example.support_service.repository.SupportMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.common.service.CommonUserService.getMyUserEntityId;

@Service
@RequiredArgsConstructor
public class SupportUserService {

    private final RedisService redisService;

    private final SupportChatRepository supportChatRepository;

    private final SupportMessageRepository supportMessageRepository;

    private final EmailKafkaClient emailKafkaClient;

    private final String REDIS_KEY_CHAT_LIMIT = "supportChatCreationLimit:";

    private final String REDIS_KEY_MESSAGE_LIMIT = "supportMessCreationLimit:";

    @Value("${support_agent.email}")
    private String agent_email;


    public Map<String, Long> createSupportChat(Long userId, String topic) {

         SupportChat supportChat = new SupportChat();
         supportChat.setUserId(userId);
         supportChat.setTopic(topic);
         supportChat = supportChatRepository.save(supportChat);

         redisService.saveTemp(REDIS_KEY_CHAT_LIMIT +userId,"",7200);

         Map<String, Long> result = new HashMap<>();
         result.put("chatId", supportChat.getId());
         result.put("userId", userId);

         return result;

    }

    public boolean supportChatCreationIsLimited(Long userId) {

        return redisService.get(REDIS_KEY_CHAT_LIMIT +userId)!=null;


    }
    public boolean supportMessageCreationIsLimited(Long chatId){

        return redisService.get(REDIS_KEY_MESSAGE_LIMIT +chatId)!=null;

    }

    public Optional<SupportMessage> saveSupportMessage(Long userId, Long chatId, String message, boolean isUserMessage) {

        SupportChat supportChat = supportChatRepository.findById(chatId).orElseThrow(()->new RuntimeException("chat not found"));


        if(isUserMessage){

            if(!supportChat.getUserId().equals(userId)) return Optional.empty();

            redisService.saveTemp(REDIS_KEY_MESSAGE_LIMIT +chatId,"",120);
            supportChat.setNeedsAnswer(true);
            emailKafkaClient.sendSimpleMail(agent_email,"Новое сообщение в чат поддержки № %d".formatted(chatId),"Сообщение:\n\n %s".formatted(message));
        }
        else{
            supportChat.setNeedsAnswer(false);
        }

        SupportMessage supportMessage = new SupportMessage();


        supportMessage.setMessage(message);
        supportMessage.setUserMessage(isUserMessage);
        supportMessage.setChat(supportChat);

        supportChatRepository.save(supportChat);

        return Optional.of(supportMessageRepository.save(supportMessage));


    }

    public List<SupportChat> getAllUserSupportChats(Long userId){

        return supportChatRepository.findAllByUserIdOrderByIdDesc(userId);
    }



    public List<SupportMessage> getAllSupportChatMessages(Long userId, Long chatId) {

        SupportChat supportChat = supportChatRepository.findById(chatId).orElseThrow(()->new RuntimeException("chat not found"));

        if(!supportChat.getUserId().equals(userId)){
            return List.of();
        }

        return supportMessageRepository.findAllByChatOrderById(supportChat);

    }

    public boolean deleteSupportChat(Long userId, Long chatId) throws UserNotFoundException {

        SupportChat supportChat = supportChatRepository.findById(chatId).orElseThrow(()->new RuntimeException("chat not found"));

        if(!supportChat.getUserId().equals(userId)) return false;

        supportChatRepository.delete(supportChat);

        return true;

    }


















}
