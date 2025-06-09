package com.cart.service.pricing;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecialOffersTest {
    private final RegularPricingStrategy regularStrategy = new RegularPricingStrategy();
    private final BOGOPricingStrategy bogoStrategy = new BOGOPricingStrategy();
    private final ThreeForTwoPricingStrategy threeForTwoStrategy = new ThreeForTwoPricingStrategy();
    private final BulkDiscountStrategy bulkDiscountStrategy = new BulkDiscountStrategy();
    private final SeasonalDiscountStrategy seasonalStrategy = new SeasonalDiscountStrategy();

    @Test
    void shouldCalculateMangoWithMultipleStrategies() {
        BigDecimal basePrice = new BigDecimal("1.00");
        int quantity = 5;

        // First apply bulk discount (10% off)
        BigDecimal afterBulk = bulkDiscountStrategy.calculatePrice(quantity, basePrice)
                                                   .setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("4.50"), afterBulk);
    }

    @Test
    void shouldCalculateAllSpecialOffers() {
        // BOGO for Melons (£0.50 each)
        BigDecimal melonPrice = new BigDecimal("0.50");
        assertEquals(new BigDecimal("0.50"), bogoStrategy.calculatePrice(1, melonPrice));
        assertEquals(new BigDecimal("0.50"), bogoStrategy.calculatePrice(2, melonPrice));
        assertEquals(new BigDecimal("1.00"), bogoStrategy.calculatePrice(3, melonPrice));

        // Three for Two for Limes (£0.15 each)
        BigDecimal limePrice = new BigDecimal("0.15");
        assertEquals(new BigDecimal("0.30"), threeForTwoStrategy.calculatePrice(3, limePrice));
        assertEquals(new BigDecimal("0.45"), threeForTwoStrategy.calculatePrice(4, limePrice));
        assertEquals(new BigDecimal("0.60"), threeForTwoStrategy.calculatePrice(5, limePrice));

        // Regular pricing for Apples (£0.35 each)
        BigDecimal applePrice = new BigDecimal("0.35");
        assertEquals(new BigDecimal("0.70"), regularStrategy.calculatePrice(2, applePrice));
    }

    @Test
    void shouldCalculateBulkDiscountWithDifferentQuantities() {
        BigDecimal price = new BigDecimal("1.00");

        // Less than bulk quantity - no discount
        assertEquals(new BigDecimal("3.00"),
                     bulkDiscountStrategy.calculatePrice(3, price));

        // Exact bulk quantity - 10% discount
        assertEquals(new BigDecimal("4.50"),
                     bulkDiscountStrategy.calculatePrice(5, price));

        // More than bulk quantity - 10% discount
        assertEquals(new BigDecimal("5.40"),
                     bulkDiscountStrategy.calculatePrice(6, price));
    }

    @Test
    void shouldApplySeasonalDiscountCorrectly() {
        BigDecimal price = new BigDecimal("1.00");

        // 5% off any quantity
        assertEquals(new BigDecimal("0.95"),
                     seasonalStrategy.calculatePrice(1, price));
        assertEquals(new BigDecimal("1.90"),
                     seasonalStrategy.calculatePrice(2, price));
    }
}
