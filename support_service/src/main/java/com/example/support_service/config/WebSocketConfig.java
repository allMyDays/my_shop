package com.example.support_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

    @Configuration
    @EnableWebSocketMessageBroker
    public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

        private final WebSocketJwtHandshakeInterceptor webSocketJwtHandshakeInterceptor;

        public WebSocketConfig(WebSocketJwtHandshakeInterceptor webSocketJwtHandshakeInterceptor) {
            this.webSocketJwtHandshakeInterceptor = webSocketJwtHandshakeInterceptor;
        }

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/support-chat-output-topic");
            config.setApplicationDestinationPrefixes("/support-chat-input-controller");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {

            registry.addEndpoint("/ws-support")
                    .addInterceptors(webSocketJwtHandshakeInterceptor)
                    .setAllowedOriginPatterns("*")  // разрешает все домены для подключения
                    .withSockJS();     // чтобы работало даже в старых браузерах, где нет вебсокетов.


        }

        @Override
        public void configureClientInboundChannel(ChannelRegistration registration) {

            registration.interceptors(new SecurityContextChannelInterceptor());

        }

}
