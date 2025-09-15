package com.example.email.controller.kafka;

import static com.example.common.kafka.constant.Topics.EMAIL_TOPIC;

import com.example.common.kafka.dto.email.EmailSimpleMailDTO;
import com.example.email.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailRequestsConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = EMAIL_TOPIC , groupId = "${spring.kafka.consumer.group-id}")
    public void handleEmailRequests(EmailSimpleMailDTO emailDto)  {
        emailService.sendSimpleMail(emailDto.getTo(), emailDto.getSubject(), emailDto.getText(), 1);
    }
}
