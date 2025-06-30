package com.example.managerapp.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final RedisService redisService;


    @Autowired
    public EmailService(JavaMailSender mailSender, RedisService redisService) {
        this.mailSender = mailSender;
        this.redisService = redisService;
    }

    public void sendSimpleMail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);

    }


    public void sendRandomCodeToEmail(String to) {

        String code = generate6DigitsCode();
        sendSimpleMail(to, "Подтверждение почтового ящика", "Введите в течение 15 минут этот код: "+code);

        redisService.saveTemp(to, code, 900);

    }

    private static String generate6DigitsCode(){
        Random random = new Random();
        int code = 100_000 + random.nextInt(900_000);
        return String.valueOf(code);
    }


}
