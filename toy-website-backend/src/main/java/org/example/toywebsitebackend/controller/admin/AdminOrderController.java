package org.example.toywebsitebackend.controller.admin;

import org.example.toywebsitebackend.exception.NotFoundException;
import org.example.toywebsitebackend.model.Order;
import org.example.toywebsitebackend.model.OrderItem;
import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.model.enums.OrderStatus;
import org.example.toywebsitebackend.repository.OrderItemRepository;
import org.example.toywebsitebackend.repository.OrderRepository;
import org.example.toywebsitebackend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public AdminOrderController(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listAll() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> resp = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", o.getId());
            dto.put("orderNumber", o.getOrderNumber());
            dto.put("status", o.getStatus().name());
            dto.put("total", o.getTotal());
            dto.put("createdAt", o.getCreatedAt());
            dto.put("expiresAt", o.getExpiresAt());
            dto.put("user", o.getUser() == null ? null : Map.of(
                    "id", o.getUser().getId(),
                    "email", o.getUser().getEmail(),
                    "name", o.getUser().getName(),
                    "role", o.getUser().getRole() == null ? null : o.getUser().getRole().name()
            ));
            resp.add(dto);
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        Order o = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        List<OrderItem> items = orderItemRepository.findByOrderId(id);

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", o.getId());
        dto.put("orderNumber", o.getOrderNumber());
        dto.put("status", o.getStatus().name());
        dto.put("shippingAddress", o.getShippingAddress());
        dto.put("shippingMethod", o.getShippingMethod().name());
        dto.put("shippingFee", o.getShippingFee());
        dto.put("subtotal", o.getSubtotal());
        dto.put("total", o.getTotal());
        dto.put("createdAt", o.getCreatedAt());
        dto.put("expiresAt", o.getExpiresAt());
        dto.put("user", o.getUser() == null ? null : Map.of(
                "id", o.getUser().getId(),
                "email", o.getUser().getEmail(),
                "name", o.getUser().getName(),
                "role", o.getUser().getRole() == null ? null : o.getUser().getRole().name()
        ));

        List<Map<String, Object>> itemDtos = new ArrayList<>();
        for (OrderItem i : items) {
            itemDtos.add(Map.of(
                    "id", i.getId(),
                    "productId", i.getProductId(),
                    "productName", i.getProductName(),
                    "productPrice", i.getProductPrice(),
                    "quantity", i.getQuantity(),
                    "subtotal", i.getSubtotal()
            ));
        }
        dto.put("items", itemDtos);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String statusRaw = body.get("status") == null ? null : String.valueOf(body.get("status"));
        if (statusRaw == null) throw new IllegalArgumentException("status is required");
        OrderStatus next = OrderStatus.valueOf(statusRaw);

        Order o = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        OrderStatus current = o.getStatus();

        if (current == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot change status of CANCELLED order");
        }
        if (current == OrderStatus.FULFILLED) {
            throw new IllegalArgumentException("Cannot change status of FULFILLED order");
        }

        if (next == OrderStatus.CANCELLED) {
            // Restore stock only if it was pending
            restoreStockFromOrder(o.getId());
            o.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(o);
            return ResponseEntity.ok(Map.of("message", "Order cancelled"));
        }

        if (next == OrderStatus.FULFILLED) {
            o.setStatus(OrderStatus.FULFILLED);
            orderRepository.save(o);
            return ResponseEntity.ok(Map.of("message", "Order fulfilled"));
        }

        // Keep pending or unsupported transitions
        if (next == OrderStatus.AWAITING_PAYMENT) {
            o.setExpiresAt(o.getExpiresAt() == null ? LocalDateTime.now().plusMinutes(5) : o.getExpiresAt());
            orderRepository.save(o);
            return ResponseEntity.ok(Map.of("message", "Order set to awaiting payment"));
        }

        throw new IllegalArgumentException("Unsupported status transition");
    }

    private void restoreStockFromOrder(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem i : items) {
            Product p = productRepository.findById(i.getProductId()).orElse(null);
            if (p != null) {
                int stock = p.getStock() == null ? 0 : p.getStock();
                int qty = i.getQuantity() == null ? 0 : i.getQuantity();
                p.setStock(stock + qty);
                productRepository.save(p);
            }
        }
    }
}


