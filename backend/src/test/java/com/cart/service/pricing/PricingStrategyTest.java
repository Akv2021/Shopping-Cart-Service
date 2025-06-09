package com.cart.service.pricing;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PricingStrategyTest {
    private final RegularPricingStrategy regularStrategy = new RegularPricingStrategy();
    private final BOGOPricingStrategy bogoStrategy = new BOGOPricingStrategy();
    private final ThreeForTwoPricingStrategy threeForTwoStrategy = new ThreeForTwoPricingStrategy();

    @Test
    void regularPricing() {
        BigDecimal basePrice = new BigDecimal("0.35");
        assertEquals(new BigDecimal("0.35"), regularStrategy.calculatePrice(1, basePrice));
        assertEquals(new BigDecimal("0.70"), regularStrategy.calculatePrice(2, basePrice));
    }

    @Test
    void bogoStrategyDifferentQuantities() {
        BigDecimal price = new BigDecimal("0.50");
        assertEquals(new BigDecimal("0.50"), bogoStrategy.calculatePrice(1, price));
        assertEquals(new BigDecimal("0.50"), bogoStrategy.calculatePrice(2, price));
    }

    @Test
    void threeForTwoStrategyDifferentQuantities() {
        BigDecimal price = new BigDecimal("0.15");
        assertEquals(new BigDecimal("0.30"), threeForTwoStrategy.calculatePrice(3, price));
    }
}
