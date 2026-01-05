package org.example.toywebsitebackend.controller;

import org.example.toywebsitebackend.model.Order;
import org.example.toywebsitebackend.model.enums.ShippingMethod;
import org.example.toywebsitebackend.service.OrderService;
import org.example.toywebsitebackend.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body) {
        Long userId = SecurityUtil.requireUserId();
        String shippingAddress = body.get("shippingAddress") == null ? null : String.valueOf(body.get("shippingAddress"));
        String shippingMethodRaw = body.get("shippingMethod") == null ? null : String.valueOf(body.get("shippingMethod"));
        ShippingMethod shippingMethod = shippingMethodRaw == null ? null : ShippingMethod.valueOf(shippingMethodRaw);

        Order order = orderService.createOrderFromCart(userId, shippingAddress, shippingMethod);

        Map<String, Object> resp = new HashMap<>();
        resp.put("orderId", order.getId());
        resp.put("orderNumber", order.getOrderNumber());
        resp.put("status", order.getStatus().name());
        resp.put("expiresAt", order.getExpiresAt());
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getOrders() {
        Long userId = SecurityUtil.requireUserId();
        return ResponseEntity.ok(orderService.listOrders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long id) {
        Long userId = SecurityUtil.requireUserId();
        return ResponseEntity.ok(orderService.getOrder(userId, id));
    }
}


