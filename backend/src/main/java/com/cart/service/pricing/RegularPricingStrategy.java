package com.cart.service.pricing;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("REGULAR")
public class RegularPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(int quantity, BigDecimal basePrice) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        if (quantity == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return basePrice.multiply(BigDecimal.valueOf(quantity))
                        .setScale(2, RoundingMode.HALF_UP);
    }

}