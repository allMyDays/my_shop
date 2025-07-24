package com.example.managerapp.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final RedisService redisService;

    @Value("${spring.mail.username}")
    private String emailFrom;


    @Autowired
    public EmailService(JavaMailSender mailSender, RedisService redisService) {
        this.mailSender = mailSender;
        this.redisService = redisService;
    }

    @Async
    public void sendSimpleMail(String to, String subject, String text) throws MailSendException {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(emailFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        }
        catch (MailSendException e) {
            throw new MailSendException("Не удалось отправить письмо! Убедитесь, что адрес %s существует".formatted(to));
        }


    }

    @Async
    public void sendRandomCodeToEmail(String to) throws MailSendException {

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
