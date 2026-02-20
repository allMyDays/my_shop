package com.example.email.controller.kafka;

import com.example.common.dto.email.kafka.EmailSimpleMailDTO;
import com.example.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.example.common.constant.kafka.Topics.EMAIL_REQUEST_TOPIC;

@Service
@RequiredArgsConstructor
public class EmailRequestsConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = EMAIL_REQUEST_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleEmailRequests(EmailSimpleMailDTO emailDto)  {
        emailService.sendSimpleMail(emailDto.getTo(), emailDto.getSubject(), emailDto.getText(), 1);
    }
}
