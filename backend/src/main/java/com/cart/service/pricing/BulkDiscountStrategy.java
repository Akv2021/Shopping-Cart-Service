package com.cart.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;
@Component("BULK_DISCOUNT")
public class BulkDiscountStrategy implements PricingStrategy {
    private static final int MIN_QUANTITY = 5;
    private static final BigDecimal DISCOUNT = new BigDecimal("0.90"); // 10% off

    @Override
    public BigDecimal calculatePrice(int quantity, BigDecimal basePrice) {
        if (quantity == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(quantity));
        if (quantity >= MIN_QUANTITY) {
            return total.multiply(DISCOUNT).setScale(2, RoundingMode.HALF_UP);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}

