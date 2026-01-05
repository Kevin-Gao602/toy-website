package org.example.toywebsitebackend.controller.admin;

import org.example.toywebsitebackend.exception.NotFoundException;
import org.example.toywebsitebackend.model.Order;
import org.example.toywebsitebackend.model.OrderItem;
import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.model.User;
import org.example.toywebsitebackend.model.enums.OrderStatus;
import org.example.toywebsitebackend.model.enums.Role;
import org.example.toywebsitebackend.repository.CartItemRepository;
import org.example.toywebsitebackend.repository.OrderItemRepository;
import org.example.toywebsitebackend.repository.OrderRepository;
import org.example.toywebsitebackend.repository.ProductRepository;
import org.example.toywebsitebackend.repository.UserRepository;
import org.example.toywebsitebackend.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public AdminUserController(
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

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> resp = new ArrayList<>();
        for (User u : users) {
            resp.add(Map.of(
                    "id", u.getId(),
                    "email", u.getEmail(),
                    "name", u.getName(),
                    "role", u.getRole() == null ? null : u.getRole().name(),
                    "createdAt", u.getCreatedAt()
            ));
        }
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Long currentAdminId = SecurityUtil.requireUserId();
        if (currentAdminId != null && currentAdminId.equals(id)) {
            throw new IllegalArgumentException("Admin cannot delete self");
        }
        User u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));

        // Keep at least one admin; and don't allow deleting admin accounts via this endpoint.
        if (u.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot delete admin user");
        }

        // Restore stock for pending orders, then delete orders + items
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(u.getId());
        for (Order o : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());
            if (o.getStatus() == OrderStatus.AWAITING_PAYMENT) {
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
            orderItemRepository.deleteByOrderId(o.getId());
            orderRepository.delete(o);
        }

        // Clear cart
        cartItemRepository.deleteByUserId(u.getId());

        // Delete user
        userRepository.delete(u);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}


