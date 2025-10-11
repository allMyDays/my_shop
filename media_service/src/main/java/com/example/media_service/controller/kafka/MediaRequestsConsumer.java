package com.example.media_service.controller.kafka;

import com.example.common.dto.media.kafka.DeleteMediaFilesRequestDTO;
import com.example.common.dto.media.kafka.SaveMediaFilesRequestDTO;
import com.example.common.dto.media.kafka.SavedMediaFilesResponseDTO;
import com.example.media_service.service.MinioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.common.constant.kafka.Topics.*;
import static com.example.common.constant.kafka.keys.MediaKeys.*;

@Service
@RequiredArgsConstructor
public class MediaRequestsConsumer {

    private final MinioService minioService;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    @KafkaListener(topics = {MEDIA_REQUEST_TOPIC}, groupId = "${spring.kafka.consumer.group-id}")
    public void handleMediaRequests(ConsumerRecord<String, String> record){

        String key = record.key();
        String rawJsonString = record.value();

        try{
         switch (key){
             case SAVE_MEDIA_FILES:
                 var saveFileDto = objectMapper.readValue(rawJsonString, SaveMediaFilesRequestDTO.class);
                 List<String> newFileNames =  minioService.uploadFiles(saveFileDto.getFileDataDTOs(),saveFileDto.getBucket());
                 kafkaTemplate.send(MEDIA_RESPONSE_TOPIC, SAVED_MEDIA_FILES, new SavedMediaFilesResponseDTO(newFileNames, saveFileDto.getBucket(),saveFileDto.getRequestKey()));
                 break;

             case DELETE_MEDIA_FILES:
                 var deleteFileDto = objectMapper.readValue(rawJsonString, DeleteMediaFilesRequestDTO.class);
                 minioService.deleteMultipleFiles(deleteFileDto.getFileNames());
                 break;


          }
        }
     catch (Exception e){
            //todo

     }

    }
}
