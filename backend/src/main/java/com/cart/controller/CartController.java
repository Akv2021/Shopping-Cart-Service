package com.cart.controller;

import java.util.Map;

import com.cart.exception.CartException;
import com.cart.exception.VersionConflictException;
import com.cart.model.domain.Cart;
import com.cart.model.dto.AddItemRequest;
import com.cart.model.dto.CartResponse;
import com.cart.model.dto.SyncRequest;
import com.cart.model.dto.SyncResponse;
import com.cart.service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> createCart() {
        Cart cart = cartService.createCart();
        return ResponseEntity.ok(CartResponse.from(cart));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItem(
        @PathVariable String cartId,
        @Valid @RequestBody AddItemRequest request) {
        try {
            Cart cart = cartService.getCart(cartId);
            validateVersion(cart, request.getClientVersion());

            Cart updatedCart = cartService.addItem(cart, request.getItemName());
            return ResponseEntity.ok(CartResponse.from(updatedCart));
        } catch (VersionConflictException e) {
            throw new CartException("Version conflict, please refresh", HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String cartId) {
        Cart cart = cartService.getCart(cartId);
        return ResponseEntity.ok(CartResponse.from(cart));
    }

    @DeleteMapping("/{cartId}/items/{itemName}")
    public ResponseEntity<CartResponse> removeItem(
        @PathVariable String cartId,
        @PathVariable String itemName,
        @RequestParam(required = false) Long clientVersion) {
        Cart cart = cartService.getCart(cartId);
        validateVersion(cart, clientVersion);

        Cart updatedCart = cartService.removeItem(cart, itemName);
        return ResponseEntity.ok(CartResponse.from(updatedCart));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Map<String, String>> clearCart(
        @PathVariable String cartId,
        @RequestParam(required = false) Long clientVersion) {
        Cart cart = cartService.getCart(cartId);
        validateVersion(cart, clientVersion);

        cartService.clearCart(cart);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/{cartId}/sync")
    public ResponseEntity<SyncResponse> syncOfflineOperations(
        @PathVariable String cartId,
        @RequestBody SyncRequest request) {
        SyncResponse response = cartService.syncOperations(cartId, request.getOperations());
        return ResponseEntity.ok(response);
    }

    private void validateVersion(Cart cart, Long clientVersion) {
        if (clientVersion != null && clientVersion < cart.getVersion()) {
            throw new VersionConflictException(cart.getVersion());
        }
    }
}