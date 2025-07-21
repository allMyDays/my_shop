package com.example.managerapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
//import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageSecurityMetadataSource;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.Builder;

@Configuration
@EnableWebSocketMessageBroker     // включает websocket-сообщения и брокер сообщений
//@EnableWebSocketSecurity
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) { // брокер - как сортировочная станция на почте, он не хранит сообщения, не анализирует их, а просто распределяет всё что ему пришло
        config.enableSimpleBroker("/topic"); // topic - исходящие маршруты для рассылки (как рассылка сообщений подписчикам)
        config.setApplicationDestinationPrefixes("/app");    //  app - входящие маршруты от клиента (как Post-запросы)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {  // этот метод говрит спрингу: клиенты будут подключаться к вебсокету по адресу /ws

        registry.addEndpoint("/ws")  //  создает точку подключения клиента к вебсокету, то есть клиент подключается к ws://localhost:8080/ws
                .setAllowedOriginPatterns("*")    // разрешает все домены
                .withSockJS();     //  это для того чтобы сработало даже в старых браузерах, где нет вебсокетов.


    }



    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(new SecurityContextChannelInterceptor());

    }



    /*@Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(Builder messages) {
        messages
                .nullDestMatcher().permitAll()
                .simpDestMatchers("/app/handle_agent_message").hasAnyRole("AGENT", "ADMIN")
                .simpDestMatchers("/app/handle_user_message").authenticated()
                .anyMessage().permitAll();
        return messages.build();
    }*/






    /*

    *     enableSimpleBroker("/topic") включает встроенный в спринг брокер сообщений. Он будет обслуживать все каналы начинающиеся с /topic
    *     когда сервер отправляет что-то на /topic/.... , оно попадает на всех подписчиков, которые слушают этот канал
    *
    *      setApplicationDestinationPrefixes("/app") это входящие маршруты от клиента. Всё что начинается с /app/... будет обрабатываться серверным контроллером  @MessageMapping
    *
    *      configureMessageBroker указывает как маршрутизировать сообщения - куда идут от клиента и куда от сервера
    *      registerStompEndpoints  указывает куда клиенту подключаться



    *
    *     MessageMatcherDelegatingAuthorizationManager.Builder messages - это билдер для настройки авторизации на вебсокет-сообщения. Позволяет задать какие пользователи имеют право
    *     отправлять сообщения на конкретные каналы и тд. Похож на HttpSecurity
     *
          simpDestMatchers это аналог antMatchers, но для вебсокет сообщений
    *

    *
    *
    *
    * */



}
