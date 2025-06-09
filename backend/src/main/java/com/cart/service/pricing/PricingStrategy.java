package com.cart.service.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(int quantity, BigDecimal basePrice);

    default int getPriority() {
        return 100;  // Default priority
    }
}