package com.cart.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartEvent {
    private String cartId;
    private CartEventType type;
    private String itemName;
    private int quantity;
    private BigDecimal total;
    private long version;

    public enum CartEventType {
        ITEM_ADDED,
        ITEM_REMOVED,
        CART_CLEARED,
        PRICE_UPDATED
    }
}