package com.example.common.client.rest;

import com.example.common.dto.support.SupportChatResponseDTO;
import com.example.common.dto.support.SupportMessageResponseDTO;
import com.example.common.exception.BadRequestException;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import lombok.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@ConditionalOnClass(RestClient.Builder.class)
@ConditionalOnBean(RestClient.Builder.class)
public class SupportRestClient {

    private RestClient withAuthRestClient;

    private static final ParameterizedTypeReference<List<SupportChatResponseDTO>> CHAT_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<List<SupportMessageResponseDTO>> MESSAGE_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    @Autowired
    @Lazy
    public void setRestClient(@Qualifier("authSupportRestClient") RestClient withAuthRestClient) {
        this.withAuthRestClient = withAuthRestClient;
    }


    public ResponseEntity<?> createSupportChat(@NonNull String topic){

        try {
            return withAuthRestClient
                    .post()
                    .uri("/api/support/chat/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(topic)
                    .retrieve()
                    .body(ResponseEntity.class);
        }catch (HttpClientErrorException.BadRequest exception){
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException(problemDetail);

        }

    }

    public ResponseEntity<?> supportChatCreationIsLimited(){

                return withAuthRestClient
                        .get()
                        .uri("/api/support/chat/creation_check_limit")
                        .retrieve()
                        .body(ResponseEntity.class);

    }
    public ResponseEntity<?> supportMessageSendingIsLimited(@NonNull Long chatId){
          return withAuthRestClient
                   .get()
                   .uri("/api/support/message/sending_check_limit?chatId={chatId}",chatId)
                   .retrieve()
                   .body(ResponseEntity.class);

    }

    public Optional<SupportMessageResponseDTO> saveSupportMessage(Long chatId, String message, boolean isUserMessage){


 return null; //todo

    }

    public List<SupportChatResponseDTO> getAllUserSupportChats(){

        return withAuthRestClient
                .get()
                .uri("/api/support/chat/get_all")
                .retrieve()
                .body(CHAT_TYPE_REFERENCE);

    }



    public List<SupportMessageResponseDTO> getAllSupportChatMessages(@NonNull Long chatId){
        return withAuthRestClient
                .get()
                .uri("/api/support/messages/get_all?chatId={chatId}",chatId)
                .retrieve()
                .body(MESSAGE_TYPE_REFERENCE);
    }

    public ResponseEntity<?> deleteSupportChat(@NonNull Long chatId){

        try {
            return withAuthRestClient
                            .delete()
                            .uri("/api/support/chat/delete?chatId={chatId}", chatId)
                            .retrieve()
                            .body(ResponseEntity.class);


        }catch (HttpClientErrorException.NotFound exception){
            throw new NoSuchElementException(exception);



        }


    }






}
