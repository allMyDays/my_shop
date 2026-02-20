package com.example.review_service.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public void save(@NonNull String key, @NonNull String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void saveTemp(@NonNull String key, @NonNull String value, long seconds) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }


    public Optional<String> get(@NonNull String key, boolean delete) {

        String value = stringRedisTemplate.opsForValue().get(key);

        if (delete&&value!=null) {
            delete(key);
        }

        value=value==null?null:value.trim();


        return Optional.ofNullable(value);
    }


    public void delete(@NonNull String key) {
        stringRedisTemplate.delete(key);
    }
    public void delete(@NonNull List<String> keys) {
        stringRedisTemplate.delete(keys);
    }






}
