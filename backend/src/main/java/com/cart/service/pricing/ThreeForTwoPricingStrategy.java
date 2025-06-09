package com.cart.service.pricing;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("THREE_FOR_TWO")
public class ThreeForTwoPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculatePrice(int quantity, BigDecimal basePrice) {
        if (quantity == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        int sets = quantity / 3;
        int remainder = quantity % 3;
        return basePrice.multiply(BigDecimal.valueOf(sets * 2 + remainder))
                        .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int getPriority() {
        return 1;
    }
}