package com.cart.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;
@Component("SEASONAL")
public class SeasonalDiscountStrategy implements PricingStrategy {
    private static final BigDecimal DISCOUNT = new BigDecimal("0.95"); // 5% off

    @Override
    public BigDecimal calculatePrice(int quantity, BigDecimal basePrice) {
        if (quantity == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return basePrice.multiply(BigDecimal.valueOf(quantity))
                        .multiply(DISCOUNT).setScale(2, RoundingMode.HALF_UP);
    }
}
