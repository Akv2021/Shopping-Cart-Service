package com.cart.websocket;

import com.cart.model.domain.Cart;
import com.cart.model.dto.CartResponse;
import com.cart.model.event.CartEvent;
import com.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartWebSocketHandler extends TextWebSocketHandler {
    private final CartService cartService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String cartId = extractCartId(session);
            sessions.put(cartId, session);
            sendInitialState(cartId, session);
            log.debug("WebSocket connection established for cart: {}", cartId);
        } catch (Exception e) {
            log.error("Failed to establish WebSocket connection", e);
            closeSession(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String cartId = extractCartId(session);
        sessions.remove(cartId);
        log.debug("WebSocket connection closed for cart: {}", cartId);
    }

    @EventListener
    public void handleCartEvent(CartEvent event) {
        WebSocketSession session = sessions.get(event.getCartId());
        if (session != null && session.isOpen()) {
            try {
                WebSocketMessage<?> message = new TextMessage(objectMapper.writeValueAsString(event));
                session.sendMessage(message);
                log.debug("Sent cart event: {} to cart: {}", event.getType(), event.getCartId());
            } catch (IOException e) {
                log.error("Failed to send cart event", e);
                closeSession(session);
            }
        }
    }

    private String extractCartId(WebSocketSession session) {
        return session.getUri().getPath().substring(
            session.getUri().getPath().lastIndexOf('/') + 1
        );
    }

    private void sendInitialState(String cartId, WebSocketSession session) {
        try {
            Cart cart = cartService.getCart(cartId);
            CartResponse response = CartResponse.from(cart);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            log.error("Error sending initial state for cart: {}", cartId, e);
            closeSession(session);
        }
    }

    private void closeSession(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            log.error("Error closing WebSocket session", e);
        }
    }
}
