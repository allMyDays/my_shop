package com.example.common.client.kafka;

import com.example.common.dto.media.kafka.FileDataDTO;
import com.example.common.enumeration.media_service.BucketEnum;
import com.example.common.dto.media.kafka.DeleteMediaFilesRequestDTO;
import com.example.common.dto.media.kafka.SaveMediaFilesRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.common.constant.kafka.Topics.MEDIA_REQUEST_TOPIC;
import static com.example.common.constant.kafka.keys.MediaKeys.DELETE_MEDIA_FILES;
import static com.example.common.constant.kafka.keys.MediaKeys.SAVE_MEDIA_FILES;

@Service
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class MediaKafkaClient {

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @Lazy
    public void setKafkaTemplate(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void sendSavingMediaRequest(List<MultipartFile> multipartFileList, BucketEnum bucket, String requestKey) {

        List<FileDataDTO> fileDataDTOList = multipartFileList.stream()
                .map(f->{
                            try {
                                return new FileDataDTO(f.getBytes(),f.getContentType());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        })
                .toList();


        kafkaTemplate.send(MEDIA_REQUEST_TOPIC, SAVE_MEDIA_FILES, new SaveMediaFilesRequestDTO(fileDataDTOList, bucket, requestKey));
    }

    public void deleteMedia(List<String> fileNames) {

        kafkaTemplate.send(MEDIA_REQUEST_TOPIC, DELETE_MEDIA_FILES, new DeleteMediaFilesRequestDTO(fileNames));
    }







}
