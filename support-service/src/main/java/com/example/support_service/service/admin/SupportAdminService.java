package com.example.support_service.service.admin;

import com.example.common.client.grpc.MediaGrpcClient;
import com.example.common.enumeration.media.BucketEnum;
import com.example.common.exception.EntityNotFoundException;
import com.example.common.exception.TooManyImagesToUploadException;
import com.example.common.mapper.MediaMapper;
import com.example.support_service.entity.SupportChat;
import com.example.support_service.entity.SupportMessage;
import com.example.support_service.repository.SupportChatRepository;
import com.example.support_service.repository.SupportMessageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportAdminService {

    private final SupportChatRepository supportChatRepository;

    private final SupportMessageRepository supportMessageRepository;

    private final MediaGrpcClient mediaGrpcClient;

    private final MediaMapper mediaMapper;



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

    public SupportMessage getOneSupportChatMessage(long messageId) {

        return supportMessageRepository.findById(messageId)
                .orElseThrow(()->new EntityNotFoundException(SupportMessage.class,messageId));

    }

    public SupportMessage saveSupportMessage(long chatId, @NonNull String message, List<MultipartFile> photos) {

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

        if(photos != null&&!photos.isEmpty()){
            if(photos.size() > 9){
                throw new TooManyImagesToUploadException(9);
            }
            supportMessage.setPhotoFileNames(mediaGrpcClient.uploadPhotos(mediaMapper.toPhotoDataDtoList(photos), BucketEnum.chats));
        }

        return supportMessageRepository.save(supportMessage);
    }











}
