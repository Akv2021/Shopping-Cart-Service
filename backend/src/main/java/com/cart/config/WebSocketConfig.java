package com.cart.config;

import com.cart.service.CartService;
import com.cart.websocket.CartWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    public WebSocketConfig(CartService cartService, ObjectMapper objectMapper) {
        this.cartService = cartService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cartWebSocketHandler(), "/cart-ws/{cartId}")
                .setAllowedOrigins("*");
    }

    @Bean
    public CartWebSocketHandler cartWebSocketHandler() {
        return new CartWebSocketHandler(cartService, objectMapper);
    }
}