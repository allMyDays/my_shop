package com.example.cart_wishlist.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RedisService {

    private StringRedisTemplate stringRedisTemplate;

    public Optional<String> get(@NonNull String key) {

        String value = stringRedisTemplate.opsForValue().get(key);

        value=value==null?null:value.trim();

        return Optional.ofNullable(value);
    }

    public void saveTemp(@NonNull String key, @NonNull String value, long seconds) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }

    public void save(@NonNull String key, @NonNull String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }






}
