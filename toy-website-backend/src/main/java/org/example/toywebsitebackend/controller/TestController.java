package org.example.toywebsitebackend.controller;

import org.example.toywebsitebackend.model.Order;
import org.example.toywebsitebackend.model.OrderItem;
import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.model.User;
import org.example.toywebsitebackend.model.enums.OrderStatus;
import org.example.toywebsitebackend.repository.CartItemRepository;
import org.example.toywebsitebackend.repository.OrderItemRepository;
import org.example.toywebsitebackend.repository.OrderRepository;
import org.example.toywebsitebackend.repository.ProductRepository;
import org.example.toywebsitebackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试Controller
 * 用于验证前后端连接是否正常
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public TestController(
            UserRepository userRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    ) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Backend is running!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * DEV ONLY: delete seed users and their related data (orders/order_items/cart_items).
     * Deletes: user@toy.com, admin@toy.com
     */
    @DeleteMapping("/dev/delete-seed-users")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteSeedUsers() {
        List<String> emails = List.of("user@toy.com", "admin@toy.com");

        int deletedUsers = 0;
        int deletedOrders = 0;
        int deletedOrderItems = 0;
        int deletedCartItems = 0;

        for (String email : emails) {
            User u = userRepository.findByEmail(email).orElse(null);
            if (u == null) continue;

            // Delete order_items + orders
            List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(u.getId());
            for (Order o : orders) {
                // Best-effort count (not exact across DBs) by fetching first; ok for dev endpoint.
                deletedOrderItems += orderItemRepository.findByOrderId(o.getId()).size();
                orderItemRepository.deleteByOrderId(o.getId());
                orderRepository.delete(o);
                deletedOrders += 1;
            }

            // Delete cart items
            deletedCartItems += cartItemRepository.findByUserId(u.getId()).size();
            cartItemRepository.deleteByUserId(u.getId());

            // Delete user
            userRepository.delete(u);
            deletedUsers += 1;
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("deletedUsers", deletedUsers);
        resp.put("deletedOrders", deletedOrders);
        resp.put("deletedOrderItems", deletedOrderItems);
        resp.put("deletedCartItems", deletedCartItems);
        resp.put("emails", emails);
        return ResponseEntity.ok(resp);
    }

    /**
     * DEV ONLY: keep one user and delete all other users and their related data.
     * This is useful when you have many test registrations in the DB.
     *
     * Safety:
     * - If an order is still AWAITING_PAYMENT, stock is restored before deleting the record.
     */
    @DeleteMapping("/dev/keep-only-user")
    @Transactional
    public ResponseEntity<Map<String, Object>> keepOnlyUser(@RequestParam(defaultValue = "user1@toy.com") String email) {
        User keep = userRepository.findByEmail(email).orElse(null);
        if (keep == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Keep user not found: " + email
            ));
        }

        int deletedUsers = 0;
        int deletedOrders = 0;
        int deletedOrderItems = 0;
        int deletedCartItems = 0;
        int restoredStockLines = 0;

        List<User> all = userRepository.findAll();
        for (User u : all) {
            if (u.getId() != null && u.getId().equals(keep.getId())) continue;

            // Delete orders (restore stock for pending orders)
            List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(u.getId());
            for (Order o : orders) {
                List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());
                deletedOrderItems += items.size();

                if (o.getStatus() == OrderStatus.AWAITING_PAYMENT) {
                    for (OrderItem i : items) {
                        Product p = productRepository.findById(i.getProductId()).orElse(null);
                        if (p != null) {
                            int stock = p.getStock() == null ? 0 : p.getStock();
                            int qty = i.getQuantity() == null ? 0 : i.getQuantity();
                            p.setStock(stock + qty);
                            productRepository.save(p);
                            restoredStockLines += 1;
                        }
                    }
                }

                orderItemRepository.deleteByOrderId(o.getId());
                orderRepository.delete(o);
                deletedOrders += 1;
            }

            // Delete cart items
            deletedCartItems += cartItemRepository.findByUserId(u.getId()).size();
            cartItemRepository.deleteByUserId(u.getId());

            // Delete user
            userRepository.delete(u);
            deletedUsers += 1;
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "success");
        resp.put("keptEmail", email);
        resp.put("deletedUsers", deletedUsers);
        resp.put("deletedOrders", deletedOrders);
        resp.put("deletedOrderItems", deletedOrderItems);
        resp.put("deletedCartItems", deletedCartItems);
        resp.put("restoredStockLines", restoredStockLines);
        resp.put("remainingUsers", userRepository.findAll().stream().map(User::getEmail).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);
    }
}

