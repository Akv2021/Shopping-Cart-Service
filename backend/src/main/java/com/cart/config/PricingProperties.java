package com.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "shopping")
public class PricingProperties {
    private Map<String, ItemConfig> items;
    private int defaultPriority = 100;

    @Data
    public static class ItemConfig {
        private BigDecimal basePrice;
        private List<StrategyConfig> strategies;
    }

    @Data
    public static class StrategyConfig {
        private String type;
        private Integer priority;

        public int getPriority() {
            return priority != null ? priority : 100;
        }
    }

    public List<StrategyConfig> getItemStrategies(String itemName) {
        ItemConfig config = items.get(itemName);
        if (config == null || config.getStrategies() == null || config.getStrategies().isEmpty()) {
            throw new IllegalArgumentException("No strategies defined for item: " + itemName);
        }
        return config.getStrategies();
    }
}

