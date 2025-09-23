package com.example.user_service.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@AllArgsConstructor
public class RedisService {

    private StringRedisTemplate stringRedisTemplate;

    public void save(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void saveTemp(String key, String value, long seconds) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        if (key!=null){
         stringRedisTemplate.delete(key);
        }
    }
    public void delete(List<String> keys) {
        if (keys!=null){
         stringRedisTemplate.delete(keys);
        }
    }






}
