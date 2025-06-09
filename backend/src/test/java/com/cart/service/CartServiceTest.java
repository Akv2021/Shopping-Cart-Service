package com.cart.service;

import com.cart.config.PricingProperties;
import com.cart.model.domain.Cart;
import com.cart.model.event.CartEvent;
import com.cart.repository.CartRepository;
import com.cart.service.pricing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private PricingProperties pricingProperties;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CartService cartService;
    private Map<String, PricingStrategy> strategies;

    @BeforeEach
    void setUp() {
        strategies = new HashMap<>();
        strategies.put("REGULAR", new RegularPricingStrategy());
        strategies.put("BOGO", new BOGOPricingStrategy());
        strategies.put("THREE_FOR_TWO", new ThreeForTwoPricingStrategy());

        cartService = new CartService(
            cartRepository,
            strategies,
            pricingProperties,
            eventPublisher
        );

        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void createCartShouldInitializeCorrectly() {
        Cart cart = cartService.createCart();

        assertNotNull(cart.getId());
        assertEquals(BigDecimal.ZERO, cart.getTotal());
        assertTrue(cart.getItems().isEmpty());
        assertEquals(1L, cart.getVersion());
        verify(cartRepository).save(cart);
    }

    @Test
    void addItemShouldCalculateCorrectPrices() {
        setupItemConfig("APPLE", "0.35", "REGULAR");

        Cart cart = new Cart();
        Cart updatedCart = cartService.addItem(cart, "APPLE");

        assertEquals(new BigDecimal("0.35"), updatedCart.getTotal());
        assertEquals(1, updatedCart.getItems().get("APPLE").getQuantity());
        verify(eventPublisher).publishEvent(any(CartEvent.class));
    }

    @Test
    void shouldApplyBOGOForMelons() {
        setupItemConfig("MELON", "0.50", "BOGO");

        Cart cart = new Cart();
        cart = cartService.addItem(cart, "MELON");
        assertEquals(new BigDecimal("0.50"), cart.getTotal());

        cart = cartService.addItem(cart, "MELON");
        assertEquals(new BigDecimal("0.50"), cart.getTotal());
    }

    @Test
    void shouldApplyThreeForTwoForLimes() {
        setupItemConfig("LIME", "0.15", "THREE_FOR_TWO");

        Cart cart = new Cart();
        for (int i = 0; i < 3; i++) {
            cart = cartService.addItem(cart, "LIME");
        }
        assertEquals(new BigDecimal("0.30"), cart.getTotal());
    }

    private void setupItemConfig(String itemName, String price, String strategyType) {
        PricingProperties.ItemConfig config = new PricingProperties.ItemConfig();
        config.setBasePrice(new BigDecimal(price));
        PricingProperties.StrategyConfig strategyConfig = new PricingProperties.StrategyConfig();
        strategyConfig.setType(strategyType);
        config.setStrategies(List.of(strategyConfig));

        when(pricingProperties.getItems())
            .thenReturn(Map.of(itemName, config));
        when(pricingProperties.getItemStrategies(anyString()))
            .thenReturn(config.getStrategies());
    }
}
