package com.cart.service.pricing;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("BOGO")
public class BOGOPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculatePrice(int quantity, BigDecimal basePrice) {
        if (quantity == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // For BOGO: pay for ceil(quantity/2) items
        int itemsToPay = (quantity + 1) / 2;
        return basePrice.multiply(BigDecimal.valueOf(itemsToPay))
                        .setScale(2, RoundingMode.HALF_UP);
    }
}
