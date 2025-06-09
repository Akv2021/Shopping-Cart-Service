package com.cart.model.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class Cart {
    private final String id;
    private Map<String, CartItem> items;
    private BigDecimal total;
    private long version;

    public Cart() {
        this.id = UUID.randomUUID().toString();
        this.items = new HashMap<>();
        this.total = BigDecimal.ZERO;
        this.version = 1L;
    }

    public void incrementVersion() {
        this.version++;
    }

    @Data
    public static class CartItem {
        private final String name;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public CartItem(String name, BigDecimal unitPrice) {
            this.name = name;
            this.quantity = 0;
            this.unitPrice = unitPrice;
            this.totalPrice = BigDecimal.ZERO;
        }

        public void incrementQuantity() {
            this.quantity += 1;
        }

        public void updatePrice(BigDecimal newTotalPrice) {
            this.totalPrice = newTotalPrice;
        }
    }

    public CartItem addItem(String itemName, BigDecimal unitPrice) {
        return items.computeIfAbsent(itemName,
                                     k -> new CartItem(k, unitPrice));
    }
}
