package com.cart.model.dto;

import com.cart.model.domain.Cart;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartResponse {
    private String cartId;
    private List<CartItemDTO> items = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;
    private long version;

    @Data
    public static class CartItemDTO {
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public static CartItemDTO from(Cart.CartItem item) {
            CartItemDTO dto = new CartItemDTO();
            dto.setName(item.getName());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setTotalPrice(item.getTotalPrice());
            return dto;
        }
    }

    public static CartResponse from(Cart cart) {
        if (cart == null) {
            return new CartResponse();
        }

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setTotal(cart.getTotal());
        response.setVersion(cart.getVersion());

        if (cart.getItems() != null) {
            response.setItems(cart.getItems().values().stream()
                                  .map(CartItemDTO::from)
                                  .collect(Collectors.toList()));
        }

        return response;
    }
}
