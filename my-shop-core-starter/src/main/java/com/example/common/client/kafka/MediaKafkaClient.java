package com.example.common.client.kafka;

import com.example.common.dto.media.kafka.DeleteMediaFilesRequestDTO;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.common.constant.kafka.Topics.MEDIA_REQUEST_TOPIC;
import static com.example.common.constant.kafka.keys.MediaKeys.DELETE_MEDIA_FILES;

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

    public void deleteMedia(@NonNull List<String> fileNames) {

        kafkaTemplate.send(MEDIA_REQUEST_TOPIC, DELETE_MEDIA_FILES, new DeleteMediaFilesRequestDTO(fileNames));
    }







}
