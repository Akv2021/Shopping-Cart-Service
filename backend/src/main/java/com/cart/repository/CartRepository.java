package com.cart.repository;

import com.cart.model.domain.Cart;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CartRepository {
    private final ConcurrentHashMap<String, Cart> carts = new ConcurrentHashMap<>();

    public Cart save(Cart cart) {
        carts.put(cart.getId(), cart);
        return cart;
    }

    public Optional<Cart> findById(String id) {
        return Optional.ofNullable(carts.get(id));
    }

    public void deleteById(String id) {
        carts.remove(id);
    }

    public boolean exists(String id) {
        return carts.containsKey(id);
    }
}