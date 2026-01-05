package org.example.toywebsitebackend.service;

import org.example.toywebsitebackend.model.CartItem;
import org.example.toywebsitebackend.model.Order;
import org.example.toywebsitebackend.model.OrderItem;
import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.model.User;
import org.example.toywebsitebackend.exception.NotFoundException;
import org.example.toywebsitebackend.model.enums.OrderStatus;
import org.example.toywebsitebackend.model.enums.ShippingMethod;
import org.example.toywebsitebackend.repository.CartItemRepository;
import org.example.toywebsitebackend.repository.OrderItemRepository;
import org.example.toywebsitebackend.repository.OrderRepository;
import org.example.toywebsitebackend.repository.ProductRepository;
import org.example.toywebsitebackend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order createOrderFromCart(Long userId, String shippingAddress, ShippingMethod shippingMethod) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("shippingAddress is required");
        }
        if (shippingMethod == null) {
            throw new IllegalArgumentException("shippingMethod is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate products & stock
        Map<Long, Product> productsById = new HashMap<>();
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            if (p == null || p.getId() == null) {
                throw new IllegalArgumentException("Product does not exist");
            }
            // Ensure we have a managed instance (avoid stale lazy proxies in some edge cases)
            Product managed = productRepository.findById(p.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + p.getId()));
            int qty = ci.getQuantity() == null ? 0 : ci.getQuantity();
            int stock = managed.getStock() == null ? 0 : managed.getStock();
            if (qty <= 0) {
                throw new IllegalArgumentException("Invalid quantity in cart");
            }
            if (qty > stock) {
                throw new IllegalArgumentException("Stock insufficient for product: " + managed.getName());
            }
            productsById.put(managed.getId(), managed);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = productsById.get(ci.getProduct().getId());
            BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
            int qty = ci.getQuantity();
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        BigDecimal shippingFee = shippingMethod == ShippingMethod.EXPRESS ? BigDecimal.valueOf(15) : BigDecimal.valueOf(5);
        BigDecimal total = subtotal.add(shippingFee);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setShippingAddress(shippingAddress);
        order.setShippingMethod(shippingMethod);
        order.setShippingFee(shippingFee);
        order.setSubtotal(subtotal);
        order.setTotal(total);
        order.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        Order saved = orderRepository.save(order);

        // Create order items snapshot
        for (CartItem ci : cartItems) {
            Product p = productsById.get(ci.getProduct().getId());
            BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
            int qty = ci.getQuantity();
            BigDecimal lineSubtotal = price.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(saved);
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setProductPrice(price);
            oi.setQuantity(qty);
            oi.setSubtotal(lineSubtotal);
            orderItemRepository.save(oi);
        }

        // Decrement stock
        for (CartItem ci : cartItems) {
            Product p = productsById.get(ci.getProduct().getId());
            p.setStock((p.getStock() == null ? 0 : p.getStock()) - ci.getQuantity());
            productRepository.save(p);
        }

        // Clear cart
        cartItemRepository.deleteByUserId(userId);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> resp = new ArrayList<>();
        for (Order o : orders) {
            resp.add(toOrderDto(o, orderItemRepository.findByOrderId(o.getId())));
        }
        return resp;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrder(Long userId, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (o.getUser() == null || o.getUser().getId() == null || !o.getUser().getId().equals(userId)) {
            throw new NotFoundException("Order not found");
        }
        return toOrderDto(o, orderItemRepository.findByOrderId(orderId));
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (o.getUser() == null || o.getUser().getId() == null || !o.getUser().getId().equals(userId)) {
            throw new NotFoundException("Order not found");
        }

        // Soft-delete semantics per PRD: set status to CANCELLED.
        if (o.getStatus() == OrderStatus.CANCELLED) return;
        if (o.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalArgumentException("Only pending orders can be cancelled");
        }

        restoreStockFromOrder(o.getId());
        o.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(o);
    }

    @Transactional
    public void deleteOrder(Long userId, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (o.getUser() == null || o.getUser().getId() == null || !o.getUser().getId().equals(userId)) {
            throw new NotFoundException("Order not found");
        }

        // If pending, cancel first to restore stock safely.
        if (o.getStatus() == OrderStatus.AWAITING_PAYMENT) {
            restoreStockFromOrder(o.getId());
        }

        // Remove child rows first to avoid FK constraint issues.
        orderItemRepository.deleteByOrderId(o.getId());
        orderRepository.delete(o);
    }

    private Map<String, Object> toOrderDto(Order o, List<OrderItem> items) {
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

        List<Map<String, Object>> itemDtos = new ArrayList<>();
        for (OrderItem i : items) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", i.getId());
            item.put("productId", i.getProductId());
            item.put("productName", i.getProductName());
            item.put("productPrice", i.getProductPrice());
            item.put("quantity", i.getQuantity());
            item.put("subtotal", i.getSubtotal());
            itemDtos.add(item);
        }
        dto.put("items", itemDtos);
        return dto;
    }

    private String generateOrderNumber() {
        // Example: TW-20260105-153012-4821
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        for (int attempt = 0; attempt < 5; attempt++) {
            int rnd = 1000 + new Random().nextInt(9000);
            String orderNumber = "TW-" + ts + "-" + rnd;
            if (orderRepository.findByOrderNumber(orderNumber).isEmpty()) return orderNumber;
        }
        // fallback
        return "TW-" + ts + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Auto-cancel expired orders that are still awaiting payment.
     * Runs every 30 seconds.
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void cancelExpiredOrders() {
        List<Order> expired = orderRepository.findByStatusAndExpiresAtBefore(OrderStatus.AWAITING_PAYMENT, LocalDateTime.now());
        if (expired.isEmpty()) return;

        for (Order o : expired) {
            restoreStockFromOrder(o.getId());
            o.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(o);
        }
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


