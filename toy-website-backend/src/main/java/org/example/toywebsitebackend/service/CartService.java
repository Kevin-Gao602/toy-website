package org.example.toywebsitebackend.service;

import org.example.toywebsitebackend.model.CartItem;
import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.model.User;
import org.example.toywebsitebackend.repository.CartItemRepository;
import org.example.toywebsitebackend.repository.ProductRepository;
import org.example.toywebsitebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        final BigDecimal[] subtotal = { BigDecimal.ZERO };
        List<Map<String, Object>> dtoItems = items.stream().map(ci -> {
            Product p = ci.getProduct();
            BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
            int qty = ci.getQuantity() == null ? 0 : ci.getQuantity();
            BigDecimal lineSubtotal = price.multiply(BigDecimal.valueOf(qty));
            subtotal[0] = subtotal[0].add(lineSubtotal);

            Map<String, Object> dto = new HashMap<>();
            dto.put("productId", p.getId());
            dto.put("productName", p.getName());
            dto.put("price", price);
            dto.put("quantity", qty);
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", dtoItems);
        resp.put("subtotal", subtotal[0]);
        return resp;
    }

    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId).orElse(null);
        int newQty = (item == null ? 0 : (item.getQuantity() == null ? 0 : item.getQuantity())) + quantity;
        if (newQty > (product.getStock() == null ? 0 : product.getStock())) {
            throw new IllegalArgumentException("Quantity exceeds stock");
        }

        if (item == null) {
            item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(newQty);
        } else {
            item.setQuantity(newQty);
        }
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateCartItem(Long userId, Long productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (quantity > (product.getStock() == null ? 0 : product.getStock())) {
            throw new IllegalArgumentException("Quantity exceeds stock");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeCartItem(Long userId, Long productId) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}


