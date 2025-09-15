package com.example.common.client.kafka;

import com.example.common.kafka.dto.email.EmailSimpleMailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

import static com.example.common.kafka.constant.Topics.EMAIL_TOPIC;


@Service
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class EmailKafkaClient {

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @Lazy
    public void setKafkaTemplate(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void sendSimpleMail(String to, String subject, String text) {

        kafkaTemplate.send(EMAIL_TOPIC, new EmailSimpleMailDTO(to, subject, text));
    }

}
