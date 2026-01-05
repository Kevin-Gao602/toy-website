package org.example.toywebsitebackend.controller;

import org.example.toywebsitebackend.service.CartService;
import org.example.toywebsitebackend.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart() {
        Long userId = SecurityUtil.requireUserId();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> body) {
        Long userId = SecurityUtil.requireUserId();
        Long productId = body.get("productId") == null ? null : Long.valueOf(String.valueOf(body.get("productId")));
        int quantity = body.get("quantity") == null ? 1 : Integer.parseInt(String.valueOf(body.get("quantity")));
        if (productId == null) throw new IllegalArgumentException("productId is required");
        cartService.addToCart(userId, productId, quantity);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Map<String, Object>> updateCartItem(@PathVariable Long productId, @RequestBody Map<String, Object> body) {
        Long userId = SecurityUtil.requireUserId();
        int quantity = body.get("quantity") == null ? 1 : Integer.parseInt(String.valueOf(body.get("quantity")));
        cartService.updateCartItem(userId, productId, quantity);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Map<String, Object>> removeCartItem(@PathVariable Long productId) {
        Long userId = SecurityUtil.requireUserId();
        cartService.removeCartItem(userId, productId);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart() {
        Long userId = SecurityUtil.requireUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(cartService.getCart(userId));
    }
}


