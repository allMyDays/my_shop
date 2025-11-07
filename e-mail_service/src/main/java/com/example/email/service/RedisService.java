package com.example.email.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class RedisService {

    private StringRedisTemplate stringRedisTemplate;

    public void save(@NonNull String key, @NonNull String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void saveTemp(@NonNull String key, @NonNull String value, long seconds) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }

    public String get(@NonNull String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }






}
