package com.example.email.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

   // private final RedisService redisService;

    @Value("${spring.mail.username}")
    private String storeEmail;


    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendSimpleMail(String to, String subject, String text, int attemptNum) throws MailSendException {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(storeEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        }
        catch (MailSendException e) {

            if(attemptNum < 4) {
                try {
                Thread.sleep(10_000L *(attemptNum * 2L));
             } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
             }
            sendSimpleMail(to, subject, text, ++attemptNum);
          } else{
                throw new RuntimeException("Error sending email to " + to);
            }
        }
    }


}
