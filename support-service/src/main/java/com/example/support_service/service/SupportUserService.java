package com.example.support_service.service;

import com.example.common.client.grpc.MediaGrpcClient;
import com.example.common.client.kafka.EmailKafkaClient;
import com.example.common.client.kafka.MediaKafkaClient;
import com.example.common.enumeration.media.BucketEnum;
import com.example.common.exception.*;
import com.example.common.mapper.MediaMapper;
import com.example.support_service.entity.SupportChat;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.repository.SupportChatRepository;
import com.example.support_service.repository.SupportMessageRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.support_service.enumeration.RedisSubKeys.*;

@Service
@RequiredArgsConstructor
public class SupportUserService {

    private final RedisService redisService;

    private final SupportChatRepository supportChatRepository;

    private final SupportMessageRepository supportMessageRepository;

    private final EmailKafkaClient emailKafkaClient;

    private final MediaGrpcClient mediaGrpcClient;

    private final MediaMapper mediaMapper;

    private SupportUserService selfLink;
    private MediaKafkaClient mediaKafkaClient;

    @Autowired
    @Lazy
    public void setSelfLink(SupportUserService selfLink) {
        this.selfLink = selfLink;
    }


    @Value("${support_agent.email}")
    private String agent_email;


    public Long createSupportChat(long userId, @NonNull String topic) {

         if(isChatCreationLimited(userId)){
             throw new TooManyFunctionCallsException();
         }

         SupportChat supportChat = new SupportChat();
         supportChat.setUserId(userId);
         supportChat.setTopic(topic);
         supportChat.setContainsMessages(false);
         supportChat.setNeedsAnswer(false);
         supportChat.setRead(true);
         supportChat = supportChatRepository.save(supportChat);

        Optional<String> keyOpt = redisService.get(SUPPORT_CHAT_CREATION_LIMIT +":"+userId);

         redisService.saveTemp(SUPPORT_CHAT_CREATION_LIMIT +":"+userId, keyOpt.map(s -> String.valueOf(Integer.parseInt(s) + 1)).orElse("1"),7200);

         return supportChat.getId();

    }

    public boolean isChatCreationLimited(long userId) {

        Optional<String> keyOpt = redisService.get(SUPPORT_CHAT_CREATION_LIMIT +":"+userId);
        if(keyOpt.isEmpty()) return false;

        int key = Integer.parseInt(keyOpt.get());

        return key >= 7;
    }
    public boolean isMessageSendingLimited(long chatId){

        Optional<String> keyOpt = redisService.get(SUPPORT_MESSAGE_SENDING_LIMIT +":"+chatId);
        if(keyOpt.isEmpty()) return false;

        int key = Integer.parseInt(keyOpt.get());

        return key >= 5;

    }

    public SupportMessage saveSupportMessage(long userId, long chatId, @NonNull String message, List<MultipartFile> photos) {

        SupportChat supportChat = validateChatEntityAndOwnership(userId, chatId);
        supportChat.setNeedsAnswer(true);
        supportChat.setContainsMessages(true);

        SupportMessage supportMessage = new SupportMessage();


        supportMessage.setMessage(message);
        supportMessage.setUserMessage(true);
        supportMessage.setChat(supportChat);

        if(photos != null&&!photos.isEmpty()){
            if(photos.size() > 3){
                throw new TooManyImagesToUploadException(3);
            }
            supportMessage.setPhotoFileNames(mediaGrpcClient.uploadPhotos(mediaMapper.toPhotoDataDtoList(photos), BucketEnum.chats));
        }

        supportChatRepository.save(supportChat);
        supportMessage = supportMessageRepository.save(supportMessage);

        Optional<String> keyOpt = redisService.get(SUPPORT_MESSAGE_SENDING_LIMIT +":"+chatId);
        redisService.saveTemp(SUPPORT_MESSAGE_SENDING_LIMIT +":"+chatId,
                keyOpt.map(s -> String.valueOf(Integer.parseInt(s) + 1)).orElse("1"), 120);

        emailKafkaClient.sendSimpleMail(
                agent_email,
                "Новое сообщение в чат поддержки № %d".formatted(chatId),
                "Сообщение:\n\n %s".formatted(message)
        );
        return supportMessage;
    }

    public List<SupportChat> getAllUserSupportChats(long userId){

        return supportChatRepository.findAllByUserIdOrderByIdDesc(userId);
    }

    public SupportChat getOneUserSupportChat(long userId, long chatId){
        return validateChatEntityAndOwnership(userId, chatId);
    }



    public List<SupportMessage> getAllSupportChatMessages(long userId, long chatId) {

        SupportChat supportChat = validateChatEntityAndOwnership(userId, chatId);

        return supportMessageRepository.findAllByChatOrderById(supportChat);

    }

    public SupportMessage getOneSupportChatMessage(long userId, long messageId) {

        return selfLink.validateMessageEntityAndOwnership(userId, messageId);

    }

    @Transactional
    public void deleteSupportChat(long userId, long chatId) throws UserNotFoundException {

        SupportChat supportChat = validateChatEntityAndOwnership(userId, chatId);

        List<String> imagesToDelete = new ArrayList<>();

        supportChat.getMessages().forEach(m-> {
                    if(m.getPhotoFileNames()!=null&&!m.getPhotoFileNames().isEmpty()){
                        imagesToDelete.addAll(m.getPhotoFileNames());
                    }
                   }
                );

        supportChatRepository.delete(supportChat);
        mediaKafkaClient.deleteMedia(imagesToDelete);

    }

    public Integer countUnreadSupportChats(long userId){

        return supportChatRepository.countUnreadChatsByUserId(userId);
    }

    public void markSupportChatAsRead(long userId, long chatId){

        SupportChat chat = validateChatEntityAndOwnership(userId, chatId);

        chat.setRead(true);
        supportChatRepository.save(chat);
    }

    public List<Long> getUnansweredChatIds(long userId){
        return supportChatRepository.findIdsWhereNeedsAnswerTrueByUser(userId);
    }


    public SupportChat validateChatEntityAndOwnership(long userId, long chatId) {

        SupportChat supportChat = supportChatRepository.findById(chatId)
                .orElseThrow(()->new EntityNotFoundException(SupportChat.class,chatId));

        if(!supportChat.getUserId().equals(userId)){
            throw new UserNotOwnerException(userId,supportChat.getUserId(),SupportChat.class);
        }  return supportChat;


    }

    @Transactional
    public SupportMessage validateMessageEntityAndOwnership(long userId, long messageId) {

        SupportMessage message = supportMessageRepository.findById(messageId)
                .orElseThrow(()->new EntityNotFoundException(SupportMessage.class,messageId));

        if(!message.getChat().getUserId().equals(userId)){
            throw new UserNotOwnerException(userId,message.getChat().getUserId(),SupportMessage.class);
        }  return message;

    }


    @Autowired
    public void setMediaKafkaClient(MediaKafkaClient mediaKafkaClient) {
        this.mediaKafkaClient = mediaKafkaClient;
    }
}















