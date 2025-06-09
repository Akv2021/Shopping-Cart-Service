package com.cart.model.event;

import com.cart.model.domain.Cart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CartEventTest {

    @Test
    void shouldCreateCorrectEventForAddItem() {
        Cart cart = new Cart();
        CartEvent event = new CartEvent(
            cart.getId(),
            CartEvent.CartEventType.ITEM_ADDED,
            "APPLE",
            1,
            new BigDecimal("0.35"),
            1L
        );

        assertEquals(CartEvent.CartEventType.ITEM_ADDED, event.getType());
        assertEquals("APPLE", event.getItemName());
        assertEquals(1, event.getQuantity());
        assertEquals(new BigDecimal("0.35"), event.getTotal());
        assertEquals(1L, event.getVersion());
    }

    @Test
    void shouldCreateCorrectEventForRemoveItem() {
        Cart cart = new Cart();
        CartEvent event = new CartEvent(
            cart.getId(),
            CartEvent.CartEventType.ITEM_REMOVED,
            "APPLE",
            0,
            BigDecimal.ZERO,
            2L
        );

        assertEquals(CartEvent.CartEventType.ITEM_REMOVED, event.getType());
        assertEquals("APPLE", event.getItemName());
        assertEquals(0, event.getQuantity());
        assertEquals(BigDecimal.ZERO, event.getTotal());
    }

    @Test
    void shouldCreateCorrectEventForClearCart() {
        Cart cart = new Cart();
        CartEvent event = new CartEvent(
            cart.getId(),
            CartEvent.CartEventType.CART_CLEARED,
            null,
            0,
            BigDecimal.ZERO,
            3L
        );

        assertEquals(CartEvent.CartEventType.CART_CLEARED, event.getType());
        assertEquals(BigDecimal.ZERO, event.getTotal());
        assertEquals(3L, event.getVersion());
    }
}
