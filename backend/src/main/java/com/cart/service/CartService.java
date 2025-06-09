package com.cart.service;

import com.cart.config.PricingProperties;
import com.cart.exception.CartException;
import com.cart.model.domain.Cart;
import com.cart.model.dto.SyncRequest;
import com.cart.model.dto.SyncResponse;
import com.cart.model.event.CartEvent;
import com.cart.repository.CartRepository;
import com.cart.service.pricing.PricingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final Map<String, PricingStrategy> pricingStrategies;
    private final PricingProperties pricingProperties;
    private final ApplicationEventPublisher eventPublisher;

    public Cart createCart() {
        Cart cart = new Cart();
        log.debug("Created new cart: {}", cart.getId());
        return cartRepository.save(cart);
    }

    public Cart getCart(String cartId) {
        return cartRepository.findById(cartId)
                             .orElseThrow(() -> new CartException("Cart not found: " + cartId));
    }

    public Cart addItem(Cart cart, String itemName) {
        validateItem(itemName);

        var itemConfig = pricingProperties.getItems().get(itemName);
        Cart.CartItem cartItem = cart.addItem(itemName, itemConfig.getBasePrice());
        cartItem.incrementQuantity();

        BigDecimal itemTotal = calculateItemPrice(itemName, cartItem.getQuantity());
        cartItem.updatePrice(itemTotal);

        updateCartTotal(cart);
        cart = cartRepository.save(cart);

        publishEvent(cart, CartEvent.CartEventType.ITEM_ADDED, itemName);

        return cart;
    }

    public Cart removeItem(Cart cart, String itemName) {
        Cart.CartItem item = cart.getItems().remove(itemName);
        if (item != null) {
            updateCartTotal(cart);
            cart = cartRepository.save(cart);
            publishEvent(cart, CartEvent.CartEventType.ITEM_REMOVED, itemName);
        }
        return cart;
    }

    public void clearCart(Cart cart) {
        cart.getItems().clear();
        updateCartTotal(cart);
        cart = cartRepository.save(cart);
        publishEvent(cart, CartEvent.CartEventType.CART_CLEARED, null);
    }

    public SyncResponse syncOperations(String cartId, List<SyncRequest.PendingOperation> operations) {
        Cart cart = getCart(cartId);
        int syncedCount = 0;

        for (SyncRequest.PendingOperation op : operations) {
            try {
                switch (op.getType()) {
                    case "ADD":
                        cart = addItem(cart, op.getItem());
                        syncedCount++;
                        break;
                    case "REMOVE":
                        cart = removeItem(cart, op.getItem());
                        syncedCount++;
                        break;
                    case "CLEAR":
                        clearCart(cart);
                        syncedCount++;
                        break;
                    default:
                        log.warn("Unknown operation type: {}", op.getType());
                }
            } catch (Exception e) {
                log.error("Failed to sync operation: {}", op, e);
                throw new CartException("Sync failed at operation " + syncedCount);
            }
        }

        return new SyncResponse("success", cart.getVersion(), syncedCount);
    }

    private void validateItem(String itemName) {
        if (!pricingProperties.getItems().containsKey(itemName)) {
            throw new CartException("Invalid item: " + itemName);
        }
    }

    private void updateCartTotal(Cart cart) {
        BigDecimal total = cart.getItems().values().stream()
                               .map(Cart.CartItem::getTotalPrice)
                               .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        cart.incrementVersion();
    }

    private BigDecimal calculateItemPrice(String itemName, int quantity) {
        var itemConfig = pricingProperties.getItems().get(itemName);
        if (itemConfig == null) {
            throw new CartException("Invalid item: " + itemName);
        }

        List<PricingProperties.StrategyConfig> strategies =
            pricingProperties.getItemStrategies(itemName);
        BigDecimal currentPrice = itemConfig.getBasePrice();

        // Sort strategies by priority and apply each one
        List<PricingProperties.StrategyConfig> sortedStrategies = strategies.stream()
                                                                            .sorted(
                                                                                Comparator.comparing(PricingProperties.StrategyConfig::getPriority))
                                                                            .collect(Collectors.toList());

        for (PricingProperties.StrategyConfig strategyConfig : sortedStrategies) {
            PricingStrategy strategy = pricingStrategies.get(strategyConfig.getType());
            if (strategy != null) {
                currentPrice = strategy.calculatePrice(quantity, currentPrice);
            }
        }

        return currentPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private void publishEvent(Cart cart, CartEvent.CartEventType type, String itemName) {
        CartEvent event = new CartEvent(
            cart.getId(),
            type,
            itemName,
            itemName != null ? cart.getItems().get(itemName).getQuantity() : 0,
            cart.getTotal(),
            cart.getVersion()
        );
        eventPublisher.publishEvent(event);
    }
}
