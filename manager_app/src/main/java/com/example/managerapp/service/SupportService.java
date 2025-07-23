package com.example.managerapp.service;

import com.example.managerapp.entity.MyUser;
import com.example.managerapp.entity.SupportChat;
import com.example.managerapp.entity.SupportMessage;
import com.example.managerapp.repository.SupportChatRepository;
import com.example.managerapp.repository.SupportMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final UserService userService;

    private final RedisService redisService;

    private final SupportChatRepository supportChatRepository;

    private final SupportMessageRepository supportMessageRepository;

    private final EmailService emailService;

    private final String REDIS_KEY_CHAT_LIMIT = "supportChatCreationLimit:";

    private final String REDIS_KEY_MESSAGE_LIMIT = "supportMessCreationLimit:";

    @Value("${my_shop.support_agent.email}")
    private String agent_email;


    public Map<String, Long> createSupportChat(OAuth2AuthenticationToken authentication, String topic){

         MyUser myUser = userService.getMyUserFromPostgres(authentication);

         SupportChat supportChat = new SupportChat();
         supportChat.setUser(myUser);
         supportChat.setTopic(topic);
         supportChat = supportChatRepository.save(supportChat);

         redisService.saveTemp(REDIS_KEY_CHAT_LIMIT +myUser.getId(),"",7200);

         Map<String, Long> result = new HashMap<>();
         result.put("chatId", supportChat.getId());
         result.put("userId", myUser.getId());

         return result;

    }

    public boolean isSupportChatCreationLimited(OAuth2AuthenticationToken authentication){

        MyUser myUser = userService.getMyUserFromPostgres(authentication);

        return redisService.get(REDIS_KEY_CHAT_LIMIT +myUser.getId())!=null;


    }
    public boolean isSupportMessageCreationLimited( Long chatId){

        return redisService.get(REDIS_KEY_MESSAGE_LIMIT +chatId)!=null;

    }

    public Optional<SupportMessage> saveMessage(OAuth2AuthenticationToken authenticationToken, Long chatId, String message, boolean isUserMessage){

        SupportChat supportChat = supportChatRepository.findById(chatId).orElseThrow(()->new RuntimeException("chat not found"));


        if(isUserMessage){

            if(!supportChat.getUser().equals(userService.getMyUserFromPostgres(authenticationToken))) return Optional.empty();

            redisService.saveTemp(REDIS_KEY_MESSAGE_LIMIT +chatId,"",120);
            supportChat.setNeedsAnswer(true);
            emailService.sendSimpleMail(agent_email,"Новое сообщение в чат поддержки № %d".formatted(chatId),"Сообщение:\n %s".formatted(message));
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

    public List<SupportChat> getAllUserSupportChats(OAuth2AuthenticationToken authentication, Long userId){

        MyUser myUser;

        if(userId!=null){
            myUser = userService.getMyUserFromPostgres(userId).orElseThrow(()->new RuntimeException("user not found"));
        }

        else{
            myUser = userService.getMyUserFromPostgres(authentication);
        }

        return supportChatRepository.findAllByUserOrderByIdDesc(myUser);


    }

    public List<SupportMessage> getAllChatMessages(OAuth2AuthenticationToken authentication, Long chatId){

        SupportChat supportChat = supportChatRepository.findById(chatId).orElseThrow(()->new RuntimeException("chat not found"));

        if(!userService.isUserAgentOrAdmin(authentication)&&!supportChat.getUser().equals(userService.getMyUserFromPostgres(authentication))){
            return List.of();
        }

        return supportMessageRepository.findAllByChatOrderById(supportChat);

    }

    public boolean deleteSupportChat(OAuth2AuthenticationToken authentication, Long chatId){

        MyUser myUser = userService.getMyUserFromPostgres(authentication);

        SupportChat supportChat = supportChatRepository.findById(chatId).orElseThrow(()->new RuntimeException("chat not found"));

        if(!supportChat.getUser().equals(myUser)) return false;

        supportChatRepository.delete(supportChat);

        return true;

    }

    public List<SupportChat> getAllActiveChats(){

        return supportChatRepository.findAllByNeedsAnswerTrueOrderByIdAsc();

    }

















}
