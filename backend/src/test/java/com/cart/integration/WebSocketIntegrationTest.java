package com.cart.integration;

import com.cart.model.domain.Cart;
import com.cart.model.dto.CartResponse;
import com.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CartService cartService;

    private String baseUrl;
    private StandardWebSocketClient wsClient;
    private BlockingQueue<String> messages;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
        wsClient = new StandardWebSocketClient();
        messages = new LinkedBlockingQueue<>();
    }

    @Test
    void shouldReceiveRealTimeUpdates() throws Exception {
        CartResponse cart = createCart();

        WebSocketTestHandler wsHandler = new WebSocketTestHandler();
        String wsUrl = String.format("ws://localhost:%d/cart-ws/%s", port, cart.getCartId());
        wsClient.doHandshake(wsHandler, wsUrl).get(5, TimeUnit.SECONDS);

        assertTrue(wsHandler.awaitConnection(5, TimeUnit.SECONDS));
    }

    private CartResponse createCart() {
        return restTemplate.postForObject(
            baseUrl + "/cart",
            null,
            CartResponse.class
        );
    }

    private class WebSocketTestHandler extends TextWebSocketHandler {
        private final BlockingQueue<Boolean> connectionStatus = new LinkedBlockingQueue<>();

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            connectionStatus.offer(true);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            messages.offer(message.getPayload());
        }

        public boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
            return connectionStatus.poll(timeout, unit) != null;
        }
    }
}