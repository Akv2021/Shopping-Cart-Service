package com.cart.integration;

import com.cart.model.dto.AddItemRequest;
import com.cart.model.dto.CartResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CartIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void basicCartOperations() {
        // Create cart
        ResponseEntity<CartResponse> createResponse = restTemplate.postForEntity(
            "/api/v1/cart",
            null,
            CartResponse.class
        );
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        String cartId = createResponse.getBody().getCartId();

        // Add regular item (Apple)
        addItemToCart(cartId, "APPLE");
        CartResponse cart = getCart(cartId);
        assertEquals(new BigDecimal("0.35"), cart.getTotal());

        // Add BOGO item (Melon)
        addItemToCart(cartId, "MELON");
        addItemToCart(cartId, "MELON");
        cart = getCart(cartId);
        assertEquals(new BigDecimal("0.85"), cart.getTotal());
    }

    private ResponseEntity<CartResponse> addItemToCart(String cartId, String itemName) {
        AddItemRequest request = new AddItemRequest();
        request.setItemName(itemName);
        return restTemplate.postForEntity(
            "/api/v1/cart/" + cartId + "/items",
            request,
            CartResponse.class
        );
    }

    private CartResponse getCart(String cartId) {
        return restTemplate.getForObject(
            "/api/v1/cart/" + cartId,
            CartResponse.class
        );
    }
}