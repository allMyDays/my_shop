package com.example.media_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

@Configuration
public class RedisConfig {

    @Bean
    public RedisAtomicLong redisAtomicLong(RedisConnectionFactory redisConnectionFactory) {
        return new RedisAtomicLong(
               "minio_object_id_seq" , // ключ в редис
                redisConnectionFactory,
                0  // начальное значение
        );



    }


}
