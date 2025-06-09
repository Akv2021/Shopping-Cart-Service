package com.cart.config;

import com.cart.service.CartService;
import com.cart.websocket.CartWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    @Primary
    public CartService cartService() {
        return Mockito.mock(CartService.class);
    }

    @Bean
    @Primary
    public CartWebSocketHandler cartWebSocketHandler(CartService cartService, ObjectMapper objectMapper) {
        return new CartWebSocketHandler(cartService, objectMapper);
    }
}
