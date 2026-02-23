package com.example.common.config.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;


@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@EnableKafka
public class KafkaConfig { }
