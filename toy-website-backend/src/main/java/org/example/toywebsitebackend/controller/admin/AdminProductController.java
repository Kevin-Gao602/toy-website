package org.example.toywebsitebackend.controller.admin;

import org.example.toywebsitebackend.model.Product;
import org.example.toywebsitebackend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductRepository productRepository;

    public AdminProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", productPage.getContent());
        response.put("totalElements", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        response.put("size", productPage.getSize());
        response.put("number", productPage.getNumber());
        response.put("first", productPage.isFirst());
        response.put("last", productPage.isLast());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Map<String, Object> body) {
        String name = body.get("name") == null ? null : String.valueOf(body.get("name"));
        String description = body.get("description") == null ? null : String.valueOf(body.get("description"));
        String category = body.get("category") == null ? null : String.valueOf(body.get("category"));
        String imageUrl = body.get("imageUrl") == null ? null : String.valueOf(body.get("imageUrl"));
        BigDecimal price = body.get("price") == null ? null : new BigDecimal(String.valueOf(body.get("price")));
        Integer stock = body.get("stock") == null ? 0 : Integer.parseInt(String.valueOf(body.get("stock")));

        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name is required");
        if (category == null || category.trim().isEmpty()) throw new IllegalArgumentException("category is required");
        if (price == null) throw new IllegalArgumentException("price is required");
        if (stock < 0) throw new IllegalArgumentException("stock must be >= 0");

        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setCategory(category);
        p.setPrice(price);
        p.setStock(stock);
        p.setImageUrl(imageUrl);
        return ResponseEntity.ok(productRepository.save(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Product p = opt.get();
        if (body.containsKey("name")) p.setName(String.valueOf(body.get("name")));
        if (body.containsKey("description")) p.setDescription(body.get("description") == null ? null : String.valueOf(body.get("description")));
        if (body.containsKey("category")) p.setCategory(String.valueOf(body.get("category")));
        if (body.containsKey("imageUrl")) p.setImageUrl(body.get("imageUrl") == null ? null : String.valueOf(body.get("imageUrl")));
        if (body.containsKey("price")) p.setPrice(new BigDecimal(String.valueOf(body.get("price"))));
        if (body.containsKey("stock")) {
            int stock = Integer.parseInt(String.valueOf(body.get("stock")));
            if (stock < 0) throw new IllegalArgumentException("stock must be >= 0");
            p.setStock(stock);
        }

        if (p.getName() == null || p.getName().trim().isEmpty()) throw new IllegalArgumentException("name is required");
        if (p.getCategory() == null || p.getCategory().trim().isEmpty()) throw new IllegalArgumentException("category is required");
        if (p.getPrice() == null) throw new IllegalArgumentException("price is required");

        return ResponseEntity.ok(productRepository.save(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted"));
    }
}


