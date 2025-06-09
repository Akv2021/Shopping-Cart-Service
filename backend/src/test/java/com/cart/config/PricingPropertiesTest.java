package com.cart.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
class PricingPropertiesTest {

    @Autowired
    private PricingProperties pricingProperties;

    @Test
    void shouldLoadItemWithMultipleStrategies() {
        var mango = pricingProperties.getItems().get("MANGO");
        assertNotNull(mango);

        // Compare BigDecimal with same scale
        BigDecimal expected = new BigDecimal("1.00").setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, mango.getBasePrice().setScale(2, RoundingMode.HALF_UP));

        var strategies = mango.getStrategies();
        assertEquals(2, strategies.size());

        // First strategy should be BULK_DISCOUNT with priority 1
        assertEquals("BULK_DISCOUNT", strategies.get(0).getType());
        assertEquals(1, strategies.get(0).getPriority());

        // Second strategy should be SEASONAL with priority 2
        assertEquals("SEASONAL", strategies.get(1).getType());
        assertEquals(2, strategies.get(1).getPriority());
    }

    @Test
    void shouldLoadItemsFromConfiguration() {
        assertNotNull(pricingProperties.getItems());
        assertTrue(pricingProperties.getItems().containsKey("APPLE"));
        assertTrue(pricingProperties.getItems().containsKey("MELON"));
    }

    @Test
    void shouldLoadStrategyWithDefaultPriority() {
        var apple = pricingProperties.getItems().get("APPLE");
        var strategy = apple.getStrategies().get(0);
        assertEquals(pricingProperties.getDefaultPriority(), strategy.getPriority());
    }

    @Test
    void shouldLoadStrategyWithSpecificPriority() {
        var melon = pricingProperties.getItems().get("MELON");
        var strategy = melon.getStrategies().get(0);
        assertEquals(1, strategy.getPriority());
    }

    @Test
    void shouldGetItemStrategies() {
        var strategies = pricingProperties.getItemStrategies("MELON");
        assertNotNull(strategies);
        assertEquals(1, strategies.size());
        assertEquals("BOGO", strategies.get(0).getType());
    }

    @Test
    void shouldThrowExceptionForInvalidItem() {
        assertThrows(IllegalArgumentException.class,
                     () -> pricingProperties.getItemStrategies("INVALID"));
    }
}
