package com.example.email.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

   // private final RedisService redisService;

    @Value("${spring.mail.username}")
    private String storeEmail;


    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendSimpleMail(@NonNull String to, @NonNull String subject, @NonNull String text, int attemptCounter) throws MailSendException {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(storeEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        }
        catch (MailSendException e) {

            if(attemptCounter < 4) {
                try {
                Thread.sleep(10_000L *(attemptCounter * 2L));
             } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
             }
            sendSimpleMail(to, subject, text, ++attemptCounter);
          } else{
                log.warn("Error sending email to: {}, cause: {}",to,e.getMessage());
            }
        }
    }


}
